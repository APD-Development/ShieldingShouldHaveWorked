"""Read-only source inventory for the isolated feasibility workflow."""
import pathlib
import re

root = pathlib.Path('core/src/main/java')
files = sorted(root.rglob('*.java'))
patterns = {
    'HP_WRITE': re.compile(r'\bHP\s*(?:[+*/-]?=(?!=)|\+\+|--)'),
    'DEATH_CALL': re.compile(r'\b(?:die|reallyDie)\s*\('),
    'DESTROY_CALL': re.compile(r'\b(?:hero|target|ch|enemy|defender|attacker|Dungeon\.hero)\.destroy\s*\('),
}
print('SOURCE_AUDIT_JAVA_FILES', len(files))
for path in files:
    lines = path.read_text().splitlines()
    for i, line in enumerate(lines):
        for kind, pattern in patterns.items():
            if pattern.search(line) and not line.lstrip().startswith('//'):
                print(kind, str(path).split('shatteredpixeldungeon/')[-1], i + 1, line.strip())
