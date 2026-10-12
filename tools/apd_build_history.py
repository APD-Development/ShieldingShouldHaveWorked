#!/usr/bin/env python3
"""Validate reviewed APK deltas and embed offline notes before compiling. No generated prose."""
import argparse,json,pathlib,os,re,zipfile
def validate(delta,ledger):
    if delta.get("status") not in ("ahead","identical"): raise ValueError("Previous source is not an ancestor")
    files=delta.get("files",[])
    if len(files)>=300 and not delta.get("files_complete", False): raise ValueError("Comparison may be truncated; obtain a complete diff")
    # Every changed source/test/resource/configuration/document path is covered.
    # The CI-provided git list is complete even when GitHub's API caps its diff.
    meaningful={f["filename"] for f in files}
    covered={p for c in ledger["changes"] for p in c["paths"]}
    missing=meaningful-covered
    if missing: raise ValueError("Unreviewed build changes: "+", ".join(sorted(missing)))
    if meaningful and not ledger["changes"]: raise ValueError("A changed build cannot have empty history")
    seen=set()
    for change in ledger["changes"]:
        if not change["text"] or any("\t" in change[k] or "\n" in change[k] for k in ("category","sprite","title","text")) or len(change["text"].split())>100: raise ValueError("Invalid or excessive description")
        key=(change["category"],change["text"])
        if key in seen: raise ValueError("Duplicate change description")
        seen.add(key)
        if not change["title"] or not change["sprite"]: raise ValueError("Missing title or sprite")
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
def verify_compiled(root,apk):
    assets=root/"core/src/main/assets/apd/history"
    with zipfile.ZipFile(apk) as archive:
        for path in assets.glob("*.tsv"):
            member="assets/apd/history/"+path.name
            if archive.read(member)!=path.read_bytes():
                raise ValueError("Compiled history differs from reviewed source: "+path.name)

def complete_comparison(delta,previous,current):
    """Obtain every changed path from pinned Git trees, with no rename ambiguity."""
    import subprocess
    data=subprocess.check_output(["git","diff","--name-status","--no-renames","-z",previous,current])
    fields=data.decode("utf-8").split("\0")
    if fields[-1]=="":fields.pop()
    if len(fields)%2:raise ValueError("Incomplete git diff output")
    delta=dict(delta)
    delta["files"]=[{"status":fields[i],"filename":fields[i+1]} for i in range(0,len(fields),2)]
    delta["files_complete"]=True
    return delta

if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--comparison",required=True);p.add_argument("--ledger",required=True);p.add_argument("--build",required=True);p.add_argument("--root",default=".");p.add_argument("--previous-apk");p.add_argument("--verify-apk");a=p.parse_args()
    ledger=json.loads(pathlib.Path(a.ledger).read_text());delta=json.loads(pathlib.Path(a.comparison).read_text())
    validate(delta,ledger)
    if a.previous_apk: merge_previous_apk(pathlib.Path(a.root),a.previous_apk,ledger["previous_label"].replace("TEST", "TEST ") if "TEST " not in ledger["previous_label"] else ledger["previous_label"])
    embed(pathlib.Path(a.root),ledger,a.build)
    if a.verify_apk:
        verify_compiled(pathlib.Path(a.root),pathlib.Path(a.verify_apk))
    print("Complete reviewed build changes embedded; descriptions are curated, not inferred from commit titles.")

