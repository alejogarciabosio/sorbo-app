#!/usr/bin/env bash
# Herramienta de desarrollo: toca en el teléfono conectado el elemento cuyo texto o descripción coincide con el argumento.
# Uso: scripts/tocar.sh "Guardar"      (también: --lista para ver los textos que hay en pantalla)
set -euo pipefail

export ADB="${ADB:-adb}"
if ! command -v "$ADB" >/dev/null 2>&1; then
  export ADB="$HOME/Library/Android/sdk/platform-tools/adb"
fi

if [ $# -lt 1 ]; then
  echo "Uso: $0 \"texto a tocar\"   |   $0 --lista" >&2
  exit 1
fi

VOLCADO="$(mktemp)"
trap 'rm -f "$VOLCADO"' EXIT
"$ADB" shell uiautomator dump /sdcard/pantalla.xml >/dev/null
"$ADB" pull /sdcard/pantalla.xml "$VOLCADO" >/dev/null

python3 -I - "$VOLCADO" "$1" <<'PY'
import re
import subprocess
import sys
import os
import xml.etree.ElementTree as ET

ruta, buscado = sys.argv[1], sys.argv[2]
adb = os.environ.get("ADB") or "adb"
elementos = []
for nodo in ET.parse(ruta).iter("node"):
    texto = nodo.get("text") or nodo.get("content-desc") or ""
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", nodo.get("bounds") or "")
    if texto and m:
        x1, y1, x2, y2 = map(int, m.groups())
        elementos.append((texto, (x1 + x2) // 2, (y1 + y2) // 2))

if buscado == "--lista":
    for texto, x, y in elementos:
        print(f"{x:5d},{y:5d}  {texto}")
    sys.exit(0)

coincidencias = [e for e in elementos if buscado.lower() in e[0].lower()]
if not coincidencias:
    print(f'No encontré "{buscado}" en pantalla. Probá con --lista.', file=sys.stderr)
    sys.exit(2)
texto, x, y = coincidencias[0]
if len(coincidencias) > 1:
    print(f'Hay {len(coincidencias)} coincidencias; toco la primera: "{texto}"', file=sys.stderr)
subprocess.run([adb, "shell", "input", "tap", str(x), str(y)], check=True)
print(f'Toqué "{texto}" en ({x}, {y})')
PY
