#!/usr/bin/env python3

from argparse import ArgumentParser
from json import dumps
from os.path import basename, dirname, realpath
from subprocess import PIPE
from subprocess import run as cmd
from tempfile import NamedTemporaryFile

from yaml import safe_load

cwd = dirname(realpath(__file__))

tmp_options = {
    "dir": cwd,
    "prefix": "pyrefac-test-",
    "suffix": ".json",
    "encoding": 'utf8',
    "delete": True
}

cmd_options = {
    'encoding': 'utf8',
    'cwd': cwd,
    'stdout': PIPE,
    'stderr': PIPE
}


def test_case(content, operation, path, repository, verbose):
    with NamedTemporaryFile('w', **tmp_options) as config:
        file_name = basename(config.name)
        config.write(content)
        config.flush()
        result = cmd(['./pyrefac.sh', repository, path, operation, file_name], **cmd_options)
        out, err = result.stdout, result.stderr
        color = '\033[92m' if not err else '\033[91m'
        outcome = f'{color}[{"SUCCESS" if not err else "FAILURE"}]\033[0m'
        print(outcome, repository[15:-4], path, operation, content)
        if verbose: print(out if not err else err)


def main(verbose=False):
    with open('test.yaml', 'r') as test_input_file:
        for test_input in safe_load(test_input_file):
            repository = test_input['repository']
            refactorings = test_input['refactorings']
            for refactoring in refactorings:
                path = refactoring['path']
                operation = refactoring['refactoring_function']
                parameters = refactoring['parameters']
                content = dumps(parameters)
                test_case(content, operation, path, repository, verbose)


if __name__ == '__main__':
    parser = ArgumentParser(
        description='Check PyRefac against test inputs',
        epilog='Note: Does not actually check the output of the refactorings'
    )
    parser.add_argument('-v', '--verbose', action='store_true', help='Include more detailed output')
    args = parser.parse_args()
    main(args.verbose)
