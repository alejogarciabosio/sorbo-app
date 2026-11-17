// Edge Function · eliminar la cuenta y todos los datos del usuario (tarea A5, RNF06).
// Usa la clave de servicio, que solo existe del lado servidor. La app solo manda su propio token.
import { createClient } from "jsr:@supabase/supabase-js@2";

const BUCKET = "etiquetas";

function responder(estado: number, cuerpo: unknown): Response {
  return new Response(JSON.stringify(cuerpo), {
    status: estado,
    headers: { "Content-Type": "application/json" },
  });
}

Deno.serve(async (req: Request) => {
  if (req.method !== "POST") return responder(405, { error: "metodo" });

  const url = Deno.env.get("SUPABASE_URL")!;
  const autorizacion = req.headers.get("Authorization") ?? "";

  // 1. Quién es: solo se puede borrar la cuenta propia, la que dice el token.
  const clienteUsuario = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!, {
    global: { headers: { Authorization: autorizacion } },
    auth: { persistSession: false },
  });
  const { data: { user }, error } = await clienteUsuario.auth.getUser();
  if (error || !user) return responder(401, { error: "sin_sesion" });

  const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!, { auth: { persistSession: false } });

  try {
    // 2. Fotos: todo lo que está en la carpeta del usuario ({usuario_id}/...).
    for (;;) {
      const { data: archivos, error: errorLista } = await admin.storage.from(BUCKET).list(user.id, { limit: 100 });
      if (errorLista) throw errorLista;
      if (!archivos || archivos.length === 0) break;
      const { error: errorBorrado } = await admin.storage.from(BUCKET).remove(archivos.map((a) => `${user.id}/${a.name}`));
      if (errorBorrado) throw errorBorrado;
    }

    // 3. El usuario. Sus filas (perfiles, vinos, degustaciones, uso_ia) se borran en cascada.
    const { error: errorUsuario } = await admin.auth.admin.deleteUser(user.id);
    if (errorUsuario) throw errorUsuario;

    return responder(200, { ok: true });
  } catch {
    console.error("no se pudo eliminar la cuenta");
    return responder(500, { error: "servicio" });
  }
});
