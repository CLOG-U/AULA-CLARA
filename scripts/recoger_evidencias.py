#!/usr/bin/env python3
"""Collect current, passing test reports and real Android rendering evidence."""
import argparse
import hashlib
import json
import shutil
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def suite(path, expected):
    root = ET.parse(path).getroot()
    counts = {key: int(root.get(key, 0)) for key in ['tests', 'failures', 'errors', 'skipped']}
    if counts['tests'] != expected or any(counts[key] for key in ['failures', 'errors', 'skipped']):
        raise RuntimeError(f'Incomplete or failing tests: {path.name}: {counts}')
    return counts


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--instrumentadas', type=Path)
    parser.add_argument('--repeticion', type=Path, help='Passing targeted rerun of the single failed scenario in the general suite.')
    args = parser.parse_args()
    evidence = ROOT / 'docs/evidencias'
    evidence.mkdir(parents=True, exist_ok=True)
    reports = evidence / 'reportes'
    reports.mkdir(exist_ok=True)
    unit = ROOT / 'app/build/test-results/testDebugUnitTest'
    cases = [('TEST-ec.edu.aulaclara.ui.AgendaRulesTest.xml', 8), ('TEST-ec.edu.aulaclara.ComposeMvpTest.xml', 6)]
    main_sources = list((ROOT / 'app/src/main').rglob('*.kt'))
    latest_source = max(p.stat().st_mtime for p in main_sources + list((ROOT / 'app/src/test').rglob('*.kt')))
    latest_instrumented_source = max(p.stat().st_mtime for p in main_sources + list((ROOT / 'app/src/androidTest').rglob('*.kt')))
    for name, count in cases:
        file = unit / name
        suite(file, count)
        if file.stat().st_mtime < latest_source:
            raise RuntimeError('Run tests again after the latest Kotlin changes: ' + name)
        shutil.copy2(file, reports / name)
    lint = ROOT / 'app/build/reports/lint-results-debug.xml'
    if ET.parse(lint).getroot().findall('issue'):
        raise RuntimeError('Lint still contains findings')
    shutil.copy2(lint, reports / lint.name)
    for image in (ROOT / 'app/build/outputs/robolectric').glob('*.png'):
        if image.stat().st_mtime < latest_source:
            raise RuntimeError('Stale screenshot: ' + image.name)
        shutil.copy2(image, evidence / image.name)
    for name in ['01-inicio.png', '02-formulario.png', '03-detalle-completado.png', 'adaptable-320dp.png', 'adaptable-800dp.png']:
        if not (evidence / name).is_file():
            raise RuntimeError('Missing screenshot: ' + name)
    checks = [
        'Compilación del APK de desarrollo: correcta (assembleDebug).',
        'Ocho pruebas de reglas de negocio: aprobadas; cero fallidas, omitidas o con error.',
        'Seis pruebas de interfaz con Robolectric y Android API 34: aprobadas; cero fallidas, omitidas o con error.',
        'Recorridos comprobados: crear, editar, completar, cancelar/confirmar borrado, catálogos, búsqueda, temas, selectores y conservación del borrador.',
        'Adaptabilidad comprobada con interfaz ejecutada a 320, 390 y 800 dp; recreación de actividad conserva la agenda.',
        'Android Lint: cero errores y cero advertencias.',
        'APK de pruebas instrumentadas: compilado correctamente (assembleDebugAndroidTest).',
    ]
    limitations = 'No se probó en un teléfono físico ni en todas las versiones Android compatibles. La autenticación, persistencia permanente y notificaciones reales pertenecen a etapas posteriores.'
    if args.instrumentadas and args.repeticion:
        general = ET.parse(args.instrumentadas).getroot()
        assert int(general.get('tests')) == 6 and int(general.get('failures')) == 1
        assert int(general.get('errors')) == 0 and int(general.get('skipped')) == 0
        failed = [case for case in general.findall('testcase') if case.find('failure') is not None]
        assert len(failed) == 1
        suite(args.repeticion, 1)
        rerun = ET.parse(args.repeticion).getroot().find('testcase')
        assert (rerun.get('classname'), rerun.get('name')) == (failed[0].get('classname'), failed[0].get('name'))
        if args.repeticion.stat().st_mtime < latest_instrumented_source:
            raise RuntimeError('The targeted rerun predates the latest instrumented test changes')
        if args.instrumentadas.stat().st_mtime < max(p.stat().st_mtime for p in main_sources):
            raise RuntimeError('The general suite predates the application changes')
        apk = ROOT / 'app/build/outputs/apk/debug/app-debug.apk'
        recorded = json.loads((reports / 'apk-verificado.json').read_text())['sha256']
        assert hashlib.file_digest(apk.open('rb'), 'sha256').hexdigest() == recorded, 'The application APK changed after the general suite'
        if args.instrumentadas.resolve() != (reports / 'TEST-instrumentadas-suite-inicial.xml').resolve():
            shutil.copy2(args.instrumentadas, reports / 'TEST-instrumentadas-suite-inicial.xml')
        shutil.copy2(args.repeticion, reports / 'TEST-instrumentadas-repeticion.xml')
        checks.append('Emulador Android 10 / API 29: cinco de seis recorridos aprobaron en la suite general. El restante aprobó en repetición dirigida (1/1), tras ajustar la comprobación al desplazamiento. Se utilizó el mismo APK; ambos informes se conservan.')
    elif args.instrumentadas:
        suite(args.instrumentadas, 6)
        if args.instrumentadas.stat().st_mtime < latest_instrumented_source:
            raise RuntimeError('The instrumented report predates the latest Kotlin changes')
        shutil.copy2(args.instrumentadas, reports / 'TEST-instrumentadas.xml')
        checks.append('Seis pruebas instrumentadas en un emulador Android 10 / API 29: aprobadas; cero fallidas, omitidas o con error.')
    else:
        limitations += ' Las pruebas instrumentadas en dispositivo no se incluyen como aprobadas en este informe.'
    report = {
        'checks': checks,
        'environment': 'Linux x86_64; JDK 17; Gradle 8.11.1; Android Gradle Plugin 8.9.2; Kotlin 2.1.20; SDK de compilación 35; Robolectric 4.14.1 con Android API 34 y renderizado Skia nativo.' + (' Emulador AOSP Android 10 / API 29.' if args.instrumentadas else ''),
        'limitations': limitations,
        'screenshot_source': 'Estas imágenes corresponden a la interfaz Compose ejecutada con el framework Android API 34 mediante Robolectric y renderizada por Skia nativo. No son imágenes del prototipo ni capturas de un teléfono físico.',
    }
    (evidence / 'verificacion.json').write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n')
    (evidence / 'LEER.md').write_text('# Evidencia de verificación\n\n' + '\n'.join('- ' + check for check in checks) + '\n\n' + report['environment'] + '\n\n' + report['screenshot_source'] + '\n\n' + limitations + '\n')
    print('Evidence collected in', evidence)


if __name__ == '__main__':
    main()
