#!/usr/bin/env python3
"""Package tracked project files and selected delivery artifacts, without caches or keys."""
import argparse
import hashlib
import subprocess
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--salida', type=Path, required=True)
    args = parser.parse_args()
    out = args.salida.resolve()
    out.mkdir(parents=True, exist_ok=True)
    commit = subprocess.check_output(['git', 'rev-parse', '--verify', 'HEAD'], cwd=ROOT, text=True).strip()
    tracked = subprocess.check_output(['git', 'ls-files', '-z'], cwd=ROOT).decode().split('\0')
    tracked = [name for name in tracked if name]
    project = out / 'Aula_Clara_Proyecto_Android.zip'
    with ZipFile(project, 'w', ZIP_DEFLATED) as archive:
        for name in tracked:
            archive.write(ROOT / name, 'AulaClara/' + name)
    instructions = (
        'AULA CLARA · ENTREGA DE ETAPA 2\n\n'
        'Integrante único: Carlos Luis Ortiz García.\n'
        'Repositorio: https://github.com/CLOG-U/AULA-CLARA\n'
        f'Commit del proyecto: {commit}\n\n'
        'WORD PRINCIPAL: Aula_Clara_Etapas_1_y_2.docx. Incluye las 32 páginas de la etapa 1 y la etapa 2.\n'
        'La etapa 1 se recibió en PDF y se conserva como imágenes fieles; la etapa 2 es editable.\n'
        'Aula_Clara_Etapa_2.docx contiene solo la sección nueva para integrarla en el Word original, si se dispone de él.\n'
        'Los PDF permiten revisar la presentación; la consigna solicita entregar el Word en Moodle.\n\n'
        'ANDROID STUDIO: abrir AulaClara/ (o descomprimir el ZIP de proyecto), sincronizar con JDK 17 y SDK 35 y ejecutar app.\n'
        'APK: Aula_Clara_MVP.apk es una compilación de desarrollo para Android API 26 o posterior.\n'
        'Para revisar el ejemplo, pulsar Explorar demostración.\n'
        'Acceso y avisos son demostrativos. El cierre de sesión limpia los datos; no hay almacenamiento permanente.\n'
        'El README y docs/GUIA_DEFENSA.md explican el código y el recorrido. docs/evidencias/ conserva los resultados técnicos.\n'
    )
    (out / 'LEER_ENTREGA.txt').write_text(instructions)
    names = ['Aula_Clara_Etapas_1_y_2.docx', 'Aula_Clara_Etapas_1_y_2.pdf',
             'Aula_Clara_Etapa_2.docx', 'Aula_Clara_Etapa_2.pdf', 'Aula_Clara_MVP.apk',
             'Aula_Clara_Etapa_1_Original.pdf', 'Aula_Clara_Proyecto_Android.zip']
    for name in names:
        if not (out / name).is_file():
            raise FileNotFoundError(name)
    hashes = [hashlib.file_digest((out / name).open('rb'), 'sha256').hexdigest() + '  ' + name for name in names]
    (out / 'SHA256SUMS.txt').write_text('\n'.join(hashes) + '\n')
    bundle = out / 'Aula_Clara_Entrega_Completa.zip'
    with ZipFile(bundle, 'w', ZIP_DEFLATED) as archive:
        for name in tracked:
            archive.write(ROOT / name, 'AulaClara/' + name)
        for name in names:
            if name != project.name:
                archive.write(out / name, 'Entrega/' + name)
        archive.write(out / 'LEER_ENTREGA.txt', 'LEER_ENTREGA.txt')
        bundle_hashes = [hashlib.file_digest((out / name).open('rb'), 'sha256').hexdigest() + '  Entrega/' + name
                         for name in names if name != project.name]
        archive.writestr('SHA256SUMS.txt', '\n'.join(bundle_hashes) + '\n')
    for file in [project, bundle]:
        with ZipFile(file) as archive:
            assert archive.testzip() is None, 'Invalid ZIP: ' + file.name
        print(file.name, file.stat().st_size, 'bytes')


if __name__ == '__main__':
    main()
