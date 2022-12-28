#!/usr/bin/python3

import glob
import os
import subprocess
import sys

PROJ_DIR = os.path.dirname(os.path.realpath(__file__))
DUMP_DIR = os.path.join(PROJ_DIR, "tmp")
TESTS_DIR = os.path.join(PROJ_DIR, "programs/source")
GRADLE_OUTPUT_DIR = os.path.join(PROJ_DIR, "build/distributions")
VERSION_NAME = "compiler-playground-1.0-SNAPSHOT"
ZIP_FILE_NAME = f"{VERSION_NAME}.zip"
ZIP_FILE = os.path.join(GRADLE_OUTPUT_DIR, ZIP_FILE_NAME)
LAUNCHER = os.path.join(PROJ_DIR, f"{VERSION_NAME}/bin/compiler-playground")


class FailedProc:
    def __init__(self):
        self.returncode = 1


def exec_cmd(cmd, fail_on_error=False):
    try:
        proc = subprocess.run(cmd, shell=True, universal_newlines=True, timeout=3)
        if fail_on_error and proc.returncode != 0:
            print(f"Command {cmd} failed...")
            sys.exit(1)
        return proc
    except subprocess.TimeoutExpired:
        return FailedProc()


def build_project():
    exec_cmd(f"{PROJ_DIR}/gradlew build", fail_on_error=True)
    exec_cmd(f"unzip -u {ZIP_FILE}", fail_on_error=True)


def compile_and_run(source_file):
    file_name = os.path.basename(os.path.realpath(source_file))
    asm_file = os.path.join(DUMP_DIR, f"{file_name}.s")
    binary = os.path.join(DUMP_DIR, f"{file_name}.out")
    print("Linking")
    res = exec_cmd(f"gcc {asm_file} -o {binary}")
    if res.returncode != 0:
        return False
    print("Running compiled binary")
    return exec_cmd(binary).returncode == 0


def run_test(source_file):
    print(f"Running {source_file}")

    # interpreter
    print("Interpreter")
    proc = exec_cmd(f"{LAUNCHER} {source_file}")
    if proc.returncode != 0:
        return False

    # compilation
    return compile_and_run(source_file)


def main():
    build_project()
    pattern = sys.argv[1] if len(sys.argv) == 2 else ""
    source_files = glob.glob(f"{TESTS_DIR}/*.prog")
    failed = []
    for file in source_files:
        if pattern not in file:
            continue
        if not run_test(file):
            failed.append(file)
    if failed:
        print("Failed tests:", file=sys.stderr)
        for test in failed:
            print(test, file=sys.stderr)

    else:
        print("All tests passed :)")


if __name__ == "__main__":
    main()
