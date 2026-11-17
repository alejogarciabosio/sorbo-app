// Edge Function · lectura de la etiqueta con un modelo de visión (mejora opcional de RF03, tarea A4).
// La app siempre lee primero con ML Kit en el teléfono; esta función solo se usa si hay conexión, cuenta y consentimiento.
// La clave de Groq vive como secreto (GROQ_API_KEY); jamás va en la app ni en el repo. No se guarda la foto ni la respuesta.
import { createClient } from "jsr:@supabase/supabase-js@2";

const LIMITE_DIARIO = 20; // lecturas por usuario y por día (el plan gratuito de Groq es chico y se comparte entre todos)
const MAX_BASE64 = 1_000_000; // la app manda la foto reducida (hasta ~500 KB, que en base64 son ~700 KB)
const ESPERA_GROQ_MS = 12_000;
const URL_GROQ = "https://api.groq.com/openai/v1";
// Modelos con visión en orden de preferencia; se cambian sin tocar el código con el secreto GROQ_VISION_MODELS.
const MODELOS_VISION = ["qwen/qwen3.8-27b", "meta-llama/llama-4-scout-17b-16e-instruct"];

class ErrorHttp extends Error {
  constructor(public estado: number, public codigo: string) {
    super(codigo);
  }
}

function responder(estado: number, cuerpo: unknown): Response {
  return new Response(JSON.stringify(cuerpo), { status: estado, headers: { "Content-Type": "application/json" } });
}

const PROMPT =
  `Esta es la foto de una botella de vino. Hay UNA botella principal (la que está en primer plano, sostenida por la mano): leé solo su etiqueta e ignorá otras botellas, papeles o textos del fondo. Devolvé solo un JSON con estas claves: nombre (la marca o nombre del vino tal como se lee, sin la cepa ni el año), bodega (el productor; si la etiqueta solo muestra una marca y no hay otro productor, repetí esa marca), cepa (la variedad de uva, p. ej. Malbec; si hay dos, separalas con ' y '), anio (número), lineas (lista con todas las líneas de texto que leés en esa etiqueta, de la más grande a la más chica). Si un dato no figura o no se lee con claridad, poné null. No inventes nada: es mejor null que adivinar.`;

const texto = (x: unknown, max: number): string | null =>
  typeof x === "string" && x.trim() ? x.trim().slice(0, max) : null;

type Lectura = { nombre: string | null; bodega: string | null; cepa: string | null; anio: number | null; lineas: string[] };

// No se confía a ciegas en lo que devuelve el modelo: se valida la forma y se acota el largo de todo.
function validarSalida(salida: Record<string, unknown>): Lectura {
  const anio = typeof salida.anio === "number" ? salida.anio : Number(salida.anio);
  return {
    nombre: texto(salida.nombre, 80),
    bodega: texto(salida.bodega, 80),
    cepa: texto(salida.cepa, 60),
    anio: Number.isInteger(anio) && anio >= 1900 && anio <= 2100 ? anio : null,
    lineas: Array.isArray(salida.lineas)
      ? salida.lineas.map((l) => texto(l, 100)).filter((l): l is string => l !== null).slice(0, 30)
      : [],
  };
}

type Intento = { ok: true; valor: Lectura } | { ok: false };

async function intentar(modelo: string, clave: string, imagenBase64: string): Promise<Intento> {
  const cuerpo: Record<string, unknown> = {
    model: modelo,
    temperature: 0,
    max_completion_tokens: 700,
    response_format: { type: "json_object" },
    messages: [{
      role: "user",
      content: [
        { type: "text", text: PROMPT },
        { type: "image_url", image_url: { url: `data:image/jpeg;base64,${imagenBase64}` } },
      ],
    }],
  };
  if (modelo.startsWith("qwen")) cuerpo.reasoning_effort = "none";

  let respuesta: Response;
  try {
    respuesta = await fetch(`${URL_GROQ}/chat/completions`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${clave}` },
      body: JSON.stringify(cuerpo),
      signal: AbortSignal.timeout(ESPERA_GROQ_MS),
    });
  } catch (e) {
    console.error("Groq no respondió:", e instanceof Error ? e.name : "error");
    throw new ErrorHttp(504, "servicio");
  }
  if (respuesta.status === 429) throw new ErrorHttp(429, "limite");
  if (!respuesta.ok) {
    const detalle = (await respuesta.text()).toLowerCase();
    console.error("Groq respondió", respuesta.status, "con el modelo", modelo);
    // Un modelo retirado responde 404, o 400 con un mensaje que nombra al modelo: se prueba el siguiente.
    if (respuesta.status === 404 || (respuesta.status === 400 && detalle.includes("model"))) return { ok: false };
    throw new ErrorHttp(502, "servicio");
  }
  try {
    const datos = await respuesta.json();
    return { ok: true, valor: validarSalida(JSON.parse(datos?.choices?.[0]?.message?.content)) };
  } catch {
    console.error("respuesta mal formada con el modelo", modelo);
    throw new ErrorHttp(502, "servicio");
  }
}

async function leerConGroq(imagenBase64: string): Promise<Lectura> {
  const clave = Deno.env.get("GROQ_API_KEY");
  if (!clave) {
    console.error("falta el secreto GROQ_API_KEY");
    throw new ErrorHttp(503, "servicio");
  }
  const configurados = (Deno.env.get("GROQ_VISION_MODELS") ?? "").split(",").map((m) => m.trim()).filter(Boolean);
  for (const modelo of configurados.length ? configurados : MODELOS_VISION) {
    const r = await intentar(modelo, clave, imagenBase64);
    if (r.ok) return r.valor;
  }
  console.error("no hay ningún modelo con visión disponible");
  throw new ErrorHttp(503, "servicio");
}

Deno.serve(async (req: Request) => {
  try {
    if (req.method !== "POST") throw new ErrorHttp(405, "metodo");

    // 1. Quién es: el token del usuario viaja en Authorization.
    const url = Deno.env.get("SUPABASE_URL")!;
    const clienteUsuario = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!, {
      global: { headers: { Authorization: req.headers.get("Authorization") ?? "" } },
      auth: { persistSession: false },
    });
    const { data: { user }, error } = await clienteUsuario.auth.getUser();
    if (error || !user) throw new ErrorHttp(401, "sin_sesion");

    // 2. Forma del pedido: una imagen JPEG en base64 y de tamaño acotado (sin consumir cupo si está mal).
    let cuerpo: Record<string, unknown>;
    try {
      cuerpo = await req.json();
    } catch {
      throw new ErrorHttp(400, "pedido_invalido");
    }
    const imagen = cuerpo?.imagen;
    if (typeof imagen !== "string" || imagen.length < 100 || imagen.length > MAX_BASE64 || !/^\/9j\/[A-Za-z0-9+/=]+$/.test(imagen)) {
      throw new ErrorHttp(400, "pedido_invalido");
    }

    // 3. Cupo por usuario y por día.
    const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!, { auth: { persistSession: false } });
    const { data: hayCupo, error: errorCupo } = await admin.rpc("consumir_cupo_ia", {
      p_usuario: user.id,
      p_limite: LIMITE_DIARIO,
      p_tipo: "etiqueta",
    });
    if (errorCupo) {
      console.error("no se pudo consultar el cupo");
      throw new ErrorHttp(503, "servicio");
    }
    if (!hayCupo) throw new ErrorHttp(429, "limite");

    // 4. Lectura. No se guarda la foto ni la respuesta.
    return responder(200, await leerConGroq(imagen));
  } catch (e) {
    if (e instanceof ErrorHttp) return responder(e.estado, { error: e.codigo });
    console.error("error inesperado");
    return responder(500, { error: "servicio" });
  }
});
