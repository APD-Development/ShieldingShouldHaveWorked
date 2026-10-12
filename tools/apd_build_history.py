#!/usr/bin/env python3
"""Validate reviewed APK deltas and embed offline notes before compiling. No generated prose."""
import argparse,json,pathlib,os,re,zipfile
def validate(delta,ledger):
    if delta.get("status") not in ("ahead","identical"): raise ValueError("Previous source is not an ancestor")
    files=delta.get("files",[])
    if len(files)>=300: raise ValueError("Comparison may be truncated; obtain a complete diff")
    meaningful={f["filename"] for f in files if (f["filename"].startswith(("core/src/main/","SPD-classes/src/main/","android/src/main/","services/src/main/")) or f["filename"] in {"build.gradle","gradle.properties","core/build.gradle","SPD-classes/build.gradle","android/build.gradle","services/build.gradle"})}
    covered={p for c in ledger["changes"] for p in c["paths"]}
    missing=meaningful-covered
    if missing: raise ValueError("Unreviewed APK changes: "+", ".join(sorted(missing)))
    for change in ledger["changes"]:
        if not change["text"] or "\n" in change["text"] or len(change["text"].split())>100: raise ValueError("Invalid or excessive description")
    return meaningful
def merge_previous_apk(root,apk,expected_label):
    """Preserve compiled history between APKs; reviewed corrections already in source win."""
    assets=root/"core/src/main/assets/apd/history"
    assets.mkdir(parents=True,exist_ok=True)
    index=assets/"index.tsv"
    current=index.read_text().splitlines() if index.exists() else []
    existing={line.split("\t")[0] for line in current if line}
    with zipfile.ZipFile(apk) as archive:
        previous=archive.read("assets/apd/history/index.tsv").decode("utf-8").splitlines()
        if not any(line.split("\t")[1]==expected_label for line in previous if len(line.split("\t"))==4):
            raise ValueError("Previous APK does not contain the expected build record")
        missing=[]
        for line in previous:
            fields=line.split("\t")
            if len(fields)!=4 or not re.fullmatch(r"[a-z0-9-]+",fields[0]):
                raise ValueError("Invalid previous APK history identifier")
            ident=fields[0]
            if ident not in existing: missing.append(line);existing.add(ident)
            target=assets/(ident+".tsv")
            member="assets/apd/history/"+ident+".tsv"
            if not target.exists() and member in archive.namelist():
                target.write_bytes(archive.read(member))
    index.write_text("\n".join(missing+current)+"\n")

def embed(root,ledger,number):
    assets=root/"core/src/main/assets/apd/history"
    assets.mkdir(parents=True,exist_ok=True)
    ident="codex-test-"+str(number)
    rows=["\t".join([c["category"],c["sprite"],c["title"],c["text"]]) for c in ledger["changes"]]
    (assets/(ident+".tsv")).write_text("\n".join(rows)+"\n")
    index=assets/"index.tsv"
    lines=[l for l in index.read_text().splitlines() if not l.startswith(ident+"\t")]
    index.write_text(ident+"\tCodex TEST "+str(number)+"\t"+ledger["date"]+"\tAPK change record"+"\n"+"\n".join(lines)+"\n")
if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--comparison",required=True);p.add_argument("--ledger",required=True);p.add_argument("--build",required=True);p.add_argument("--root",default=".");p.add_argument("--previous-apk");a=p.parse_args()
    ledger=json.loads(pathlib.Path(a.ledger).read_text());delta=json.loads(pathlib.Path(a.comparison).read_text())
    validate(delta,ledger)
    if a.previous_apk: merge_previous_apk(pathlib.Path(a.root),a.previous_apk,ledger["previous_label"].replace("TEST", "TEST ") if "TEST " not in ledger["previous_label"] else ledger["previous_label"])
    embed(pathlib.Path(a.root),ledger,a.build)
    print("Reviewed APK changes embedded; descriptions are curated, not inferred from commit titles.")

