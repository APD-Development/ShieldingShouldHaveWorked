"""Compare real APD saves, allowing only a bijective renaming of missile set IDs.

MissileWeapon.setID uses SecureRandom, outside the gameplay PRNG. Set IDs are
NOT discarded: sharing relationships and paired upgrade/enchantment maps must
remain identical. Every other JSON field and every native filename is strict.
This reads saved engine output; it does not model or execute gameplay.
"""
import gzip
import json
import sys
from pathlib import Path


class Difference(AssertionError):
    pass


def compare_documents(expected, actual):
    forward, reverse = {}, {}
    pairs = {"set_ids": "set_levels", "set_ids_enchants": "set_enchants"}
    paired_fields = set(pairs) | set(pairs.values())

    def require(condition, path, message):
        if not condition:
            raise Difference(f"{path}: {message}")

    def associate(old, new, path):
        require(type(old) is int and type(new) is int, path, "noninteger missile ID")
        # Zero and class-name hashes have explicit legacy/dart semantics.
        if abs(old) <= 2**31 or abs(new) <= 2**31:
            require(old == new, path, "reserved/legacy missile ID changed")
        require(old not in forward or forward[old] == new, path, "one missile set split")
        require(new not in reverse or reverse[new] == old, path, "distinct missile sets merged")
        forward[old], reverse[new] = new, old

    def walk(a, b, path, trackers=False):
        require(type(a) is type(b), path, "JSON type differs")
        if isinstance(a, dict):
            require(a.keys() == b.keys(), path, "object fields differ")
            is_tracker = a.get("__className", "").endswith("MissileWeapon$UpgradedSetTracker")
            if is_tracker and trackers:
                for ids, values in pairs.items():
                    if ids not in a:
                        continue
                    require(values in a, path, "missing paired tracker values")
                    require(len(a[ids]) == len(a[values]), path, "broken expected ID/value pairs")
                    require(len(b[ids]) == len(b[values]), path, "broken actual ID/value pairs")
                    require(len(set(a[ids])) == len(a[ids]), path, "duplicate expected tracker ID")
                    require(len(set(b[ids])) == len(b[ids]), path, "duplicate actual tracker ID")
                    require(all(i in forward for i in a[ids]), path, "tracker references an unmapped missile group")
                    remapped = {forward[i]: v for i, v in zip(a[ids], a[values])}
                    require(remapped == dict(zip(b[ids], b[values])), path, "paired upgrade/enchantment map differs")
            for key in sorted(a):
                if is_tracker and key in paired_fields:
                    continue
                if key == "set_id":
                    if not trackers:
                        associate(a[key], b[key], path + "/" + key)
                else:
                    walk(a[key], b[key], path + "/" + key, trackers)
        elif isinstance(a, list):
            require(len(a) == len(b), path, "array length differs")
            for i, (x, y) in enumerate(zip(a, b)):
                walk(x, y, path + "/" + str(i), trackers)
        else:
            require(a == b, path, f"value differs: {a!r} != {b!r}")

    walk(expected, actual, "native")
    walk(expected, actual, "native", trackers=True)
    return {"missile_groups": len(forward), "renamed_groups": sum(a != b for a, b in forward.items())}


def compare_dirs(expected, actual):
    def read(root):
        return {p.name: json.loads(gzip.decompress(p.read_bytes())) for p in sorted(Path(root).glob("*.dat"))}
    a, b = read(expected), read(actual)
    if not a or a.keys() != b.keys():
        raise Difference(f"native file sets differ: {sorted(a)} != {sorted(b)}")
    result = compare_documents(a, b)
    result.update(native_files=sorted(a), scope="all native JSON fields, up to a global missile-set-ID bijection; PRNG checked separately")
    return result


def self_test():
    from copy import deepcopy
    tracker = "com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon$UpgradedSetTracker"
    a, b, x, y = 10**15, 2*10**15, 3*10**15, 4*10**15
    old = {"items": [{"set_id": a, "durability": 80}, {"set_id": a, "durability": 40}, {"set_id": b, "durability": 90}],
           "tracker": {"__className": tracker, "set_ids": [a, b], "set_levels": [1, 3], "set_ids_enchants": [a], "set_enchants": ["Sacrificial"]}}
    new = {"items": [{"set_id": x, "durability": 80}, {"set_id": x, "durability": 40}, {"set_id": y, "durability": 90}],
           "tracker": {"__className": tracker, "set_ids": [y, x], "set_levels": [3, 1], "set_ids_enchants": [x], "set_enchants": ["Sacrificial"]}}
    compare_documents(old, new)
    mutations = [
        lambda j: j["items"][1].update(set_id=y),
        lambda j: j["items"][2].update(set_id=x),
        lambda j: j["items"][0].update(durability=79),
        lambda j: j["tracker"].update(set_levels=[1, 3]),
        lambda j: j["tracker"].update(set_enchants=["Projecting"]),
        lambda j: j["items"][0].update(set_id=0),
    ]
    for mutate in mutations:
        corrupt = deepcopy(new)
        mutate(corrupt)
        try:
            compare_documents(old, corrupt)
        except Difference:
            continue
        raise AssertionError("corrupt native state was accepted")
    print("NATIVE_EQUIVALENCE_SELF_TEST_PASS: renaming/order accepted; six gameplay/identity corruptions rejected")


if __name__ == "__main__":
    if sys.argv[1:] == ["--self-test"]:
        self_test()
    else:
        try:
            result = compare_dirs(*sys.argv[1:3])
            print("NATIVE_EQUIVALENCE_PASS " + json.dumps(result, sort_keys=True))
        except Difference as exc:
            print("NATIVE_EQUIVALENCE_FAIL " + str(exc))
            sys.exit(1)
