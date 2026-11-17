// Edge Function · resumen de gustos con IA (RF04, tarea A3).
// Recibe SOLO estadísticas (categorías y cantidades), nunca fotos, nombres de vinos ni notas.
// La clave de Groq vive como secreto de esta función (GROQ_API_KEY); jamás va en la app ni en el repo.
import { createClient } from "jsr:@supabase/supabase-js@2";

const LIMITE_DIARIO = 5; // resúmenes por usuario y por día
const MINIMO_DEGUSTACIONES = 5;
const ESPERA_GROQ_MS = 15_000;
const MAX_FILAS = 6;

// Valores permitidos por tipo: los mismos nombres que los enums de la app. Así no entra texto libre al prompt.
const VALORES: Record<string, string[]> = {
  dulzor: ["DULCE", "SECO"],
  comida: ["CARNE", "PASTA", "PESCADO", "PIZZA", "QUESOS", "PICADA", "POSTRE"],
  compania: ["SOLO", "PAREJA", "AMIGOS", "FAMILIA", "TRABAJO"],
  ocasion: ["CENA", "ASADO", "PREVIA", "ALMUERZO", "CELEBRACION"],
};

type Fila = { tipo: string; valor: string; favorables: number; total: number };
type Pedido = { totalDegustaciones: number; disfrutas: Fila[]; noConvence: Fila[] };

class ErrorHttp extends Error {
  constructor(public estado: number, public codigo: string) {
    super(codigo);
  }
}

function responder(estado: number, cuerpo: unknown): Response {
  return new Response(JSON.stringify(cuerpo), {
    status: estado,
    headers: { "Content-Type": "application/json" },
  });
}

const esEntero = (x: unknown, min: number, max: number): x is number =>
  typeof x === "number" && Number.isInteger(x) && x >= min && x <= max;

function validarFilas(filas: unknown): Fila[] {
  if (!Array.isArray(filas) || filas.length > MAX_FILAS) throw new ErrorHttp(400, "pedido_invalido");
  return filas.map((f) => {
    const { tipo, valor, favorables, total } = (f ?? {}) as Record<string, unknown>;
    if (typeof tipo !== "string" || typeof valor !== "string" || !VALORES[tipo]?.includes(valor)) {
      throw new ErrorHttp(400, "pedido_invalido");
    }
    if (!esEntero(total, 1, 100000) || !esEntero(favorables, 0, total)) throw new ErrorHttp(400, "pedido_invalido");
    return { tipo, valor, favorables, total };
  });
}

function validarPedido(cuerpo: unknown): Pedido {
  const c = (cuerpo ?? {}) as Record<string, unknown>;
  if (!esEntero(c.totalDegustaciones, 0, 100000)) throw new ErrorHttp(400, "pedido_invalido");
  if (c.totalDegustaciones < MINIMO_DEGUSTACIONES) throw new ErrorHttp(422, "pocas_degustaciones");
  return {
    totalDegustaciones: c.totalDegustaciones,
    disfrutas: validarFilas(c.disfrutas),
    noConvence: validarFilas(c.noConvence),
  };
}

const lineaDeFila = (f: Fila) => `- ${f.tipo} ${f.valor}: le gustaron ${f.favorables} de ${f.total}`;

function armarMensajeUsuario(p: Pedido): string {
  const disfruta = p.disfrutas.length ? p.disfrutas.map(lineaDeFila).join("\n") : "- (sin datos suficientes)";
  const noConvence = p.noConvence.length ? p.noConvence.map(lineaDeFila).join("\n") : "- (sin datos suficientes)";
  return `Degustaciones totales: ${p.totalDegustaciones}\n\nLo que más disfruta:\n${disfruta}\n\nLo que no lo convence:\n${noConvence}`;
}

const INSTRUCCIONES = `Sos un sommelier amable que habla en español rioplatense. Recibís estadísticas del diario de vinos de una persona (cantidades y porcentajes; no tenés sus notas ni los nombres de los vinos). Escribí un resumen de 2 o 3 oraciones sobre qué estilo de vinos le gusta, y de 2 a 4 sugerencias concretas de qué probar a continuación (cepas, estilos o zonas, no marcas). Usá solo lo que está en las estadísticas; si algo no se puede afirmar, no lo afirmes. No hagas diagnósticos ni recomendaciones de salud. Respondé solo un JSON con las claves "resumen" (texto) y "sugerencias" (lista de textos).`;

// Modelos en orden de preferencia. Se pueden cambiar sin tocar el código con el secreto GROQ_MODELS (separados por coma).
// Groq da de baja modelos con frecuencia: si uno ya no existe, se prueba el siguiente y, si ninguno sirve,
// se elige uno vigente de la lista que informa Groq. Así el resumen no deja de funcionar por un modelo retirado.
const MODELOS_PREFERIDOS = ["qwen/qwen3.8-27b", "llama-3.3-70b-versatile", "openai/gpt-oss-20b"];
const NO_SON_DE_TEXTO = /whisper|guard|tts|orpheus|embed|playai|safeguard/i;
const URL_GROQ = "https://api.groq.com/openai/v1";

let cacheModelos: { ids: string[]; hasta: number } | null = null;

async function modelosVigentes(clave: string): Promise<string[]> {
  if (cacheModelos && cacheModelos.hasta > Date.now()) return cacheModelos.ids;
  try {
    const r = await fetch(`${URL_GROQ}/models`, {
      headers: { Authorization: `Bearer ${clave}` },
      signal: AbortSignal.timeout(5_000),
    });
    if (!r.ok) return [];
    const datos = await r.json();
    const ids: string[] = (datos?.data ?? [])
      // deno-lint-ignore no-explicit-any
      .filter((m: any) => m?.active !== false && typeof m?.id === "string")
      // deno-lint-ignore no-explicit-any
      .map((m: any) => m.id as string)
      .filter((id: string) => !NO_SON_DE_TEXTO.test(id));
    cacheModelos = { ids, hasta: Date.now() + 10 * 60_000 };
    return ids;
  } catch {
    return [];
  }
}

type Resultado = { resumen: string; sugerencias: string[] };
type Intento = { ok: true; valor: Resultado } | { ok: false; modeloInvalido: boolean };

async function intentarConModelo(modelo: string, clave: string, p: Pedido): Promise<Intento> {
  const cuerpo: Record<string, unknown> = {
    model: modelo,
    temperature: 0.4,
    max_completion_tokens: 800,
    response_format: { type: "json_object" },
    messages: [
      { role: "system", content: INSTRUCCIONES },
      { role: "user", content: armarMensajeUsuario(p) },
    ],
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
    // Un modelo retirado responde 404, o 400 con un mensaje que nombra al modelo.
    const texto = (await respuesta.text()).toLowerCase();
    const modeloInvalido = respuesta.status === 404 || (respuesta.status === 400 && texto.includes("model"));
    console.error("Groq respondió", respuesta.status, "con el modelo", modelo);
    if (modeloInvalido) return { ok: false, modeloInvalido: true };
    throw new ErrorHttp(502, "servicio");
  }

  // No se confía a ciegas en lo que devuelve el modelo: se valida la forma y se acota el largo.
  try {
    const datos = await respuesta.json();
    const salida = JSON.parse(datos?.choices?.[0]?.message?.content);
    const resumen = typeof salida.resumen === "string" ? salida.resumen.trim().slice(0, 600) : "";
    const sugerencias = Array.isArray(salida.sugerencias)
      ? salida.sugerencias.filter((s: unknown) => typeof s === "string" && s.trim()).slice(0, 4)
        .map((s: string) => s.trim().slice(0, 120))
      : [];
    if (!resumen || sugerencias.length === 0) throw new Error("respuesta incompleta");
    return { ok: true, valor: { resumen, sugerencias } };
  } catch {
    console.error("respuesta de Groq mal formada con el modelo", modelo);
    throw new ErrorHttp(502, "servicio");
  }
}

async function pedirAGroq(p: Pedido): Promise<Resultado> {
  const clave = Deno.env.get("GROQ_API_KEY");
  if (!clave) {
    console.error("falta el secreto GROQ_API_KEY");
    throw new ErrorHttp(503, "servicio");
  }
  const configurados = (Deno.env.get("GROQ_MODELS") ?? Deno.env.get("GROQ_MODEL") ?? "")
    .split(",").map((m) => m.trim()).filter(Boolean);
  const probados: string[] = [];

  for (const modelo of configurados.length ? configurados : MODELOS_PREFERIDOS) {
    probados.push(modelo);
    const r = await intentarConModelo(modelo, clave, p);
    if (r.ok) return r.valor;
  }

  // Ningún modelo preferido sirve: se prueban hasta 2 de los vigentes que informa Groq.
  const vigentes = (await modelosVigentes(clave)).filter((m) => !probados.includes(m)).slice(0, 2);
  for (const modelo of vigentes) {
    console.error("usando un modelo alternativo:", modelo);
    const r = await intentarConModelo(modelo, clave, p);
    if (r.ok) return r.valor;
  }
  console.error("no hay ningún modelo disponible");
  throw new ErrorHttp(503, "servicio");
}

Deno.serve(async (req: Request) => {
  try {
    if (req.method !== "POST") throw new ErrorHttp(405, "metodo");

    // 1. Quién es: el token del usuario viaja en Authorization.
    const autorizacion = req.headers.get("Authorization") ?? "";
    const url = Deno.env.get("SUPABASE_URL")!;
    const clienteUsuario = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!, {
      global: { headers: { Authorization: autorizacion } },
      auth: { persistSession: false },
    });
    const { data: { user }, error } = await clienteUsuario.auth.getUser();
    if (error || !user) throw new ErrorHttp(401, "sin_sesion");

    // 2. Forma del pedido (sin consumir cupo si está mal).
    let cuerpo: unknown;
    try {
      cuerpo = await req.json();
    } catch {
      throw new ErrorHttp(400, "pedido_invalido");
    }
    const pedido = validarPedido(cuerpo);

    // 3. Cupo por usuario y por día.
    const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!, { auth: { persistSession: false } });
    const { data: hayCupo, error: errorCupo } = await admin.rpc("consumir_cupo_ia", {
      p_usuario: user.id,
      p_limite: LIMITE_DIARIO,
    });
    if (errorCupo) {
      console.error("no se pudo consultar el cupo");
      throw new ErrorHttp(503, "servicio");
    }
    if (!hayCupo) throw new ErrorHttp(429, "limite");

    // 4. IA. No se guarda nada de lo recibido ni de lo devuelto.
    return responder(200, await pedirAGroq(pedido));
  } catch (e) {
    if (e instanceof ErrorHttp) return responder(e.estado, { error: e.codigo });
    console.error("error inesperado");
    return responder(500, { error: "servicio" });
  }
});
