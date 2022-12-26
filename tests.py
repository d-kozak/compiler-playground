#!/usr/bin/python3

import glob
import subprocess

test_dir = "./programs/source"

gradle_output = "./build/distributions"

zip_file_name = "compiler-playground-1.0-SNAPSHOT.zip"

launcher = "./compiler-playground-1.0-SNAPSHOT/bin/compiler-playground"


def exec_cmd(cmd):
    return subprocess.call(cmd, shell=True, universal_newlines=True)


def build_project():
    exec_cmd("gradle build")
    exec_cmd(f"unzip -u {gradle_output}/{zip_file_name}")


def run_test(source_file):
    print(f"Running {source_file}")
    exec_cmd(f"{launcher} {source_file}")


def main():
    build_project()
    source_files = glob.glob(f"{test_dir}/*.prog")
    for file in source_files:
        run_test(file)


if __name__ == "__main__":
    main()
