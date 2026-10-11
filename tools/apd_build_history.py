#!/usr/bin/env python3
"""Validate reviewed APK deltas and embed offline notes before compiling. No generated prose."""
import argparse,json,pathlib,os
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
def embed(root,ledger,number):
    assets=root/"core/src/main/assets/apd/history"
    assets.mkdir(parents=True,exist_ok=True)
    ident="codex-test-"+str(number)
    rows=["\t".join([c["category"],c["sprite"],c["title"],c["text"]]) for c in ledger["changes"]]
    (assets/(ident+".tsv")).write_text("\n".join(rows)+"\n")
    index=assets/"index.tsv"
    lines=[l for l in index.read_text().splitlines() if not l.startswith(ident+"\t")]
    index.write_text(ident+"\tCodex TEST "+str(number)+"\t"+ledger["date"]+"\tChanges since "+ledger["previous_label"]+"\n"+"\n".join(lines)+"\n")
if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--comparison",required=True);p.add_argument("--ledger",required=True);p.add_argument("--build",required=True);p.add_argument("--root",default=".");a=p.parse_args()
    ledger=json.loads(pathlib.Path(a.ledger).read_text());delta=json.loads(pathlib.Path(a.comparison).read_text())
    validate(delta,ledger);embed(pathlib.Path(a.root),ledger,a.build)
    print("Reviewed APK changes embedded; descriptions are curated, not inferred from commit titles.")

