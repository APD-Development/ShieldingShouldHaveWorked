#!/usr/bin/env python3
"""Render the maintained APD bug report, with optional build evidence, as a PDF."""
import argparse
from datetime import datetime
from pathlib import Path
from xml.sax.saxutils import escape
from zoneinfo import ZoneInfo

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('source', type=Path)
    parser.add_argument('output', type=Path)
    parser.add_argument('--verification', type=Path)
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle('ReportBody', fontName='Helvetica', fontSize=10,
                              leading=14, spaceAfter=8, alignment=TA_LEFT,
                              textColor=colors.HexColor('#253044')))
    styles['Heading1'].textColor = colors.HexColor('#173d65')
    styles['Heading2'].textColor = colors.HexColor('#173d65')
    story = []
    prepared = datetime.now(ZoneInfo('America/New_York')).strftime('%B %d, %Y at %I:%M %p %Z')
    story.append(Paragraph('ADHD Pixel Dungeon', styles['Title']))
    story.append(Paragraph('Bug fixes, causes and possible side effects', styles['Heading1']))
    story.append(Paragraph('Private development report. Prepared ' + escape(prepared), styles['ReportBody']))
    if args.verification:
        story.append(Paragraph('Build verification', styles['Heading2']))
        for paragraph in args.verification.read_text().split('\n\n'):
            story.append(Paragraph(escape(paragraph).replace('\n', '<br/>'), styles['ReportBody']))
    for block in args.source.read_text().strip().split('\n\n'):
        if block.startswith('# '):
            continue
        if block.startswith('## '):
            story.append(Paragraph(escape(block[3:]), styles['Heading2']))
        else:
            story.append(Paragraph(escape(block).replace('\n', '<br/>'), styles['ReportBody']))
    def footer(canvas, doc):
        canvas.saveState()
        canvas.setFont('Helvetica', 8)
        canvas.setFillColor(colors.HexColor('#536479'))
        canvas.drawString(0.65 * inch, 0.4 * inch, 'APD private test • Automated checks are separate from physical-phone testing')
        canvas.drawRightString(7.85 * inch, 0.4 * inch, str(doc.page))
        canvas.restoreState()
    SimpleDocTemplate(str(args.output), pagesize=(8.5 * inch, 11 * inch),
                      rightMargin=0.65 * inch, leftMargin=0.65 * inch,
                      topMargin=0.65 * inch, bottomMargin=0.65 * inch,
                      title='APD bug fixes, causes and risks — October 9, 2026',
                      author='APD development').build(story, onFirstPage=footer, onLaterPages=footer)


if __name__ == '__main__':
    main()
