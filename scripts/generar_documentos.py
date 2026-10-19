#!/usr/bin/env python3
"""Create the stage-2 Word and a combined Word preserving every original stage-1 page.

Requires python-docx and PyMuPDF. Screenshots must come from the running Android app.
Usage: python scripts/generar_documentos.py --etapa1 path/to/etapa1.pdf --salida path/to/output
"""
import argparse
import json
from pathlib import Path

import fitz
from docx import Document
from docx.enum.section import WD_SECTION_START
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.opc.constants import RELATIONSHIP_TYPE
from docx.shared import Inches, Pt, RGBColor

ROOT = Path(__file__).resolve().parents[1]


def add_link(doc, text, url):
    paragraph = doc.add_paragraph()
    link = OxmlElement('w:hyperlink')
    link.set(qn('r:id'), doc.part.relate_to(url, RELATIONSHIP_TYPE.HYPERLINK, is_external=True))
    run = OxmlElement('w:r')
    properties = OxmlElement('w:rPr')
    color = OxmlElement('w:color'); color.set(qn('w:val'), '007F78')
    underline = OxmlElement('w:u'); underline.set(qn('w:val'), 'single')
    properties.append(color); properties.append(underline); run.append(properties)
    label = OxmlElement('w:t'); label.text = text; run.append(label)
    link.append(run); paragraph._p.append(link)


def configure(section):
    section.page_width = Inches(8.5)
    section.page_height = Inches(11)
    section.top_margin = section.bottom_margin = Inches(0.7)
    section.left_margin = section.right_margin = Inches(0.8)
    section.header.is_linked_to_previous = False
    section.footer.is_linked_to_previous = False
    footer = section.footer.paragraphs[0]
    footer.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    footer.text = 'Aula Clara · Pantallas y navegación · Etapa 2'


def add_stage2(doc, report, images, published=False):
    doc.core_properties.author = 'Carlos Luis Ortiz García'
    doc.core_properties.title = 'Aula Clara · Trabajo autónomo'
    doc.core_properties.subject = 'Pantallas y navegación con Jetpack Compose'
    configure(doc.sections[-1])
    normal = doc.styles['Normal']
    normal.font.name = 'Calibri'
    normal.font.size = Pt(11)
    normal.paragraph_format.space_after = Pt(8)
    for name in ['Title', 'Heading 1', 'Heading 2']:
        doc.styles[name].font.color.rgb = RGBColor.from_string('007F78')
    doc.add_heading('Aula Clara', 0)
    doc.add_paragraph('Segunda etapa · MVP de pantallas y navegación', style='Subtitle')
    for line in ['Asignatura: Desarrollo de aplicaciones móviles nativas',
                 'Carrera: Software · Nivel: 7', 'Docente: Edgardo Panchana',
                 'Estudiante e integrante único: Carlos Luis Ortiz García',
                 'Modalidad: individual']:
        doc.add_paragraph(line)
    doc.add_heading('Repositorio del proyecto', 1)
    add_link(doc, 'https://github.com/CLOG-U/AULA-CLARA', 'https://github.com/CLOG-U/AULA-CLARA')
    doc.add_paragraph('Se conserva el repositorio indicado desde la primera etapa. ' + (
        'El código fuente, las instrucciones de ejecución y las evidencias técnicas están publicados en este repositorio.' if published
        else 'El código está preparado en el checkout local; su publicación remota no está confirmada en este documento.'))
    doc.add_heading('1. Objetivo y alcance', 1)
    doc.add_paragraph('Migrar el prototipo de Aula Clara a una aplicación Android nativa construida con Kotlin y Jetpack Compose, conectar los recorridos mediante NavController y aplicar Material Design 3. El MVP permite demostrar la organización de entregas por materia y vencimiento.')
    doc.add_paragraph('La entrega representa P01–P20 mediante pantallas, estados de interfaz, diálogos y paneles. Incluye acceso y registro demostrativos, inicio, entregas, catálogos, ajustes, validación y confirmaciones.')
    doc.add_heading('2. Implementación y navegación', 1)
    table = doc.add_table(rows=1, cols=2)
    table.style = 'Light Shading Accent 1'
    table.rows[0].cells[0].text = 'Componente'
    table.rows[0].cells[1].text = 'Aplicación en Aula Clara'
    for a, b in [
        ('Jetpack Compose', 'Interfaz declarativa con formularios y componentes reutilizables; sin layouts de pantallas XML.'),
        ('Navigation Compose', 'NavHost y NavController con rutas de acceso, inicio, lista, detalle, edición, catálogos y ajustes. Identificadores como argumentos Long.'),
        ('Material Design 3', 'Tema claro, oscuro y del sistema; colores, tipografía, formas, tarjetas, campos, chips, diálogos y selectores Material.'),
        ('Estado de interfaz', 'Listas observables compartidas y rememberSaveable para conservar la sesión y los borradores al recrear la actividad.'),
        ('Ícono', 'Libro con una marca de verificación; primer plano vectorial y recursos adaptativos, incluido monocromático.'),
        ('Adaptabilidad', 'Ancho flexible, límite de lectura, desplazamiento, controles FlowRow y gestión del teclado e insets del sistema.'),
    ]:
        cells = table.add_row().cells
        cells[0].text, cells[1].text = a, b
    for row in table.rows:
        row._tr.get_or_add_trPr().append(OxmlElement('w:cantSplit'))
    table.rows[0]._tr.get_or_add_trPr().append(OxmlElement('w:tblHeader'))
    doc.add_paragraph('Flujo principal: Apertura → Acceso/Registro → Inicio → Nueva entrega → Detalle → Editar/Completar/Eliminar. La barra inferior conecta Inicio, Materias y Ajustes. Los catálogos agrupan Materias y Tipos.')
    doc.add_heading('3. Comportamientos del MVP', 1)
    for text in [
        'Crear, editar, completar, reabrir y eliminar entregas; la eliminación pide confirmación.',
        'Buscar por título, materia, tipo o notas; combinar filtros de materia y estado y elegir el orden de la lista.',
        'Crear, editar y eliminar materias y tipos; impedir el borrado si existen entregas asociadas.',
        'Conservar valores ante errores y pedir confirmación al descartar un formulario modificado.',
        'Crear la primera materia desde una entrega nueva, preservando el borrador.',
        'Mostrar pendientes de la semana de lunes a domingo e identificar vencimientos pasados.',
    ]:
        doc.add_paragraph(text, style='List Bullet')
    doc.add_heading('4. Límites de esta etapa', 1)
    doc.add_paragraph('El acceso es de demostración: valida formularios, pero no verifica identidad ni crea cuentas persistentes. No se guardan contraseñas. Los datos se mantienen durante la sesión y recreación de actividad; cerrar sesión los limpia. No hay base de datos, backend, Room, DataStore, repositorios ni ViewModel. Los avisos de 24 horas son una vista previa de preferencia, sin programación real ni permisos de notificación.')
    doc.add_paragraph('Los perfiles de usuarios y las pruebas de usabilidad de la primera etapa continúan siendo hipótesis y actividades pendientes. Las pruebas técnicas descritas aquí no equivalen a entrevistas o validación con estudiantes.')
    doc.add_heading('5. Comprobaciones técnicas', 1)
    for item in report['checks']:
        doc.add_paragraph(item, style='List Bullet')
    doc.add_paragraph('Entorno de comprobación: ' + report['environment'])
    doc.add_paragraph('Límites de verificación: ' + report['limitations'])
    doc.add_heading('6. Capturas del MVP ejecutado', 1)
    doc.add_paragraph(report['screenshot_source'])
    doc.paragraphs[-1].paragraph_format.keep_with_next = True
    captions = [('01-inicio.png', 'Figura 1. Inicio con datos de demostración y próximos vencimientos.'),
                ('02-formulario.png', 'Figura 2. Formulario de entrega con materia, tipo, fecha, hora y prioridad.'),
                ('03-detalle-completado.png', 'Figura 3. Detalle de una entrega marcada como completada.')]
    for index, (name, caption) in enumerate(captions):
        image = images / name
        if not image.is_file():
            raise FileNotFoundError('Missing real Android screenshot: ' + str(image))
        doc.add_picture(str(image), height=Inches(7.0))
        doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
        doc.paragraphs[-1].paragraph_format.page_break_before = index > 0
        doc.paragraphs[-1].paragraph_format.keep_with_next = True
        doc.add_paragraph(caption)
    heading = doc.add_heading('7. Ejecución y revisión', 1)
    heading.paragraph_format.page_break_before = True
    doc.add_paragraph('Abrir el proyecto en Android Studio, sincronizar Gradle con JDK 17, instalar SDK 35 y ejecutar app en Android API 26 o superior. Para recorrer el ejemplo, pulsar Explorar demostración. Para una agenda vacía, completar Registro o Acceso de demostración.')
    for command in ['./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug', './gradlew :app:connectedDebugAndroidTest']:
        doc.add_paragraph(command)
    doc.add_paragraph('El archivo README.md explica el alcance. docs/TRAZABILIDAD.md relaciona cada pantalla con su ruta. docs/GUIA_DEFENSA.md guía la explicación del código y el recorrido de revisión.')
    doc.add_heading('8. Referencias', 1)
    for reference in [
        'Consigna proporcionada: Trabajo Autónomo — Etapa Pantallas y Navegación, revisión de 0,75 puntos.',
        'Ortiz García, C. L. Aula Clara: definición conceptual y prototipos. Primera etapa del trabajo autónomo.',
        'Android Developers. Jetpack Compose. https://developer.android.com/compose',
        'Android Developers. Navigation with Compose. https://developer.android.com/develop/ui/compose/navigation',
        'Material Design. Material 3. https://m3.material.io/',
        'Android Developers. Adaptive icons. https://developer.android.com/develop/ui/views/launch/icon_design_adaptive',
    ]:
        doc.add_paragraph(reference)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--etapa1', type=Path, required=True)
    parser.add_argument('--salida', type=Path, required=True)
    parser.add_argument('--publicado', action='store_true', help='Use only after verifying the remote publication.')
    args = parser.parse_args()
    args.salida.mkdir(parents=True, exist_ok=True)
    evidence = ROOT / 'docs/evidencias'
    report = json.loads((evidence / 'verificacion.json').read_text())
    doc = Document()
    add_stage2(doc, report, evidence, args.publicado)
    doc.save(args.salida / 'Aula_Clara_Etapa_2.docx')
    combined = Document()
    section = combined.sections[0]
    source = fitz.open(args.etapa1)
    first = source[0].rect
    section.page_width, section.page_height = Pt(first.width), Pt(first.height)
    section.top_margin = section.bottom_margin = Pt(1)
    section.left_margin = section.right_margin = Pt(1)
    cache = args.salida / '.paginas-etapa1'
    cache.mkdir(exist_ok=True)
    for i, page in enumerate(source):
        path = cache / f'{i+1:02d}.png'
        page.get_pixmap(matrix=fitz.Matrix(1.6, 1.6)).save(path)
        paragraph = combined.add_paragraph()
        paragraph.paragraph_format.space_before = paragraph.paragraph_format.space_after = Pt(0)
        paragraph.paragraph_format.line_spacing = 1
        paragraph.paragraph_format.page_break_before = i > 0
        paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = paragraph.add_run()
        run.font.size = Pt(1)
        run.add_picture(str(path), height=Pt(first.height - 26))
    combined.add_section(WD_SECTION_START.NEW_PAGE)
    add_stage2(combined, report, evidence, args.publicado)
    combined.save(args.salida / 'Aula_Clara_Etapas_1_y_2.docx')
    (args.salida / 'LEER_ENTREGA.txt').write_text(
        'Aula_Clara_Etapas_1_y_2.docx reúne las 32 páginas originales de la etapa 1 y la etapa 2.\n'
        'Como la etapa 1 se proporcionó en PDF, sus páginas se conservaron como imágenes fieles; la etapa 2 es texto editable.\n'
        'Aula_Clara_Etapa_2.docx contiene únicamente la sección nueva editable para integrarla en el Word original si se dispone de él.\n'
        'Integrante único declarado: Carlos Luis Ortiz García.\n'
        'Revisar el estado de publicación del repositorio y subir personalmente el Word a Moodle.\n', encoding='utf-8')
    print('Generated Word documents in', args.salida)


if __name__ == '__main__':
    main()
