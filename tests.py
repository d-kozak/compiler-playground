#!/usr/bin/python3

import glob
import subprocess
import sys

test_dir = "./programs/source"

gradle_output = "./build/distributions"

zip_file_name = "compiler-playground-1.0-SNAPSHOT.zip"

launcher = "./compiler-playground-1.0-SNAPSHOT/bin/compiler-playground"


def exec_cmd(cmd):
    return subprocess.run(cmd, shell=True, universal_newlines=True)


def build_project():
    exec_cmd("gradle build")
    exec_cmd(f"unzip -u {gradle_output}/{zip_file_name}")


def run_test(source_file):
    print(f"Running {source_file}")
    proc = exec_cmd(f"{launcher} {source_file}")
    return proc.returncode == 0


def main():
    build_project()
    source_files = glob.glob(f"{test_dir}/*.prog")
    failed = []
    for file in source_files:
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
