#!/usr/bin/env python3
import argparse
import configparser
import subprocess


FLINK = ""
CLASS= ""
JAR = ""
PARALLELISM_VALUES = []
INPUT_VALUES = []
OUTPUT = ""
STATISTICS = ""
VERTICES = ""
GRAPHS = None
ITERATIONS = ""
SGF = None
DGF = None
PRE_LAYOUT_ITERATIONS = None


def main():
    parser = argparse.ArgumentParser(description='Run layout benchmark')
    parser.add_argument('config', type=str, help='path to config')

    args = parser.parse_args()
    config_path = args.config

    config = configparser.ConfigParser()
    config.read(config_path)

    build_params(config)

    for parallelism in PARALLELISM_VALUES:
        for inputpath in INPUT_VALUES:
            # build cmd string
            cmd = "{} run -p {} -c {} {} ".format(FLINK, parallelism, CLASS, JAR)
            cmd += "-input {} -output {} -statistics {} ".format(inputpath, OUTPUT, STATISTICS)
            cmd += "-vertices {} ".format(VERTICES)
            cmd += "-iterations {} ".format(ITERATIONS)

            if SGF is not None:
                cmd += "-sgf {} ".format(SGF)

            if DGF is not None:
                cmd += "-dgf {} ".format(DGF)

            if PRE_LAYOUT_ITERATIONS is not None:
                cmd += "-prelayoutiterations {} ".format(PRE_LAYOUT_ITERATIONS)

            if GRAPHS is not None:
                cmd += "-graphs {} ".format(GRAPHS)

            print('Running command:\n================\n\n' + ' ' + cmd)

            repeat = config.getint('SYSTEM', 'repeat')
            for i in range(repeat):
                print('\n+++ REPEAT {}/{} +++'.format(str(i + 1), str(repeat)))
                subprocess.check_call(cmd.split(' '))


def check_section(config, section_name) -> None:
    if config.has_section(section_name) is False:
        raise RuntimeError('Error while reading config: section "' + section_name + '" not found.')


def build_params(config) -> None:
    # read system config
    system_section_name = 'SYSTEM'
    check_section(config, system_section_name)

    if config.has_option(system_section_name, 'flink'):
        global FLINK
        flink = config.get(system_section_name, 'flink')
        FLINK = flink + "/bin/flink"
    else:
        raise RuntimeError("Error while reading config: missing flink executable.")

    if config.has_option(system_section_name, 'hadoop'):
        pass

    # read flink config
    flink_section_name = 'FLINK'
    check_section(config, flink_section_name)

    if config.has_option(flink_section_name, 'parallelism'):
        global PARALLELISM_VALUES
        parallelism = config.get(flink_section_name, 'parallelism')
        PARALLELISM_VALUES = parallelism.split(',')
    else:
        raise RuntimeError("Error while reading config: missing parallelism.")

    if config.has_option(flink_section_name, 'class'):
        global CLASS
        CLASS = config.get(flink_section_name, 'class')
    else:
        raise RuntimeError("Error while reading config: missing benchmark class.")

    if config.has_option(flink_section_name, 'jar'):
        global JAR
        JAR = config.get(flink_section_name, 'jar')
    else:
        raise RuntimeError('Error while reading config: missing jar.')

    # read benchmark config
    benchmark_section_name = 'BENCHMARK'
    check_section(config, benchmark_section_name)

    if config.has_option(benchmark_section_name, 'inputpath'):
        global INPUT_VALUES
        inputpaths = config.get(benchmark_section_name, 'inputpath')
        INPUT_VALUES = inputpaths.split(',')
    else:
        raise RuntimeError('Error while reading config: missing input path.')

    if config.has_option(benchmark_section_name, 'outputpath'):
        global OUTPUT
        OUTPUT = config.get(benchmark_section_name, 'outputpath')
    else:
        raise RuntimeError('Error while reading config: missing output path.')

    if config.has_option(benchmark_section_name, 'statisticspath'):
        global STATISTICS
        STATISTICS = config.get(benchmark_section_name, 'statisticspath')
    else:
        raise RuntimeError('Error while reading config: missing statistics path.')

    if config.has_option(benchmark_section_name, 'vertices'):
        global VERTICES
        VERTICES = config.get(benchmark_section_name, 'vertices')
    else:
        raise RuntimeError('Error while reading config: missing number of vertices.')

    if config.has_option(benchmark_section_name, 'iterations'):
        global ITERATIONS
        ITERATIONS = config.get(benchmark_section_name, 'iterations')
    else:
        raise RuntimeError('Error while reading config: missing number of iterations.')

    if config.has_option(benchmark_section_name, 'sgf'):
        global SGF
        SGF = config.get(benchmark_section_name, 'sgf')

    if config.has_option(benchmark_section_name, 'dgf'):
        global DGF
        DGF = config.get(benchmark_section_name, 'dgf')

    if config.has_option(benchmark_section_name, 'prelayoutiterations'):
        global PRE_LAYOUT_ITERATIONS
        PRE_LAYOUT_ITERATIONS = config.get(benchmark_section_name, 'prelayoutiterations')

    if config.has_option(benchmark_section_name, 'graphs'):
        global GRAPHS
        GRAPHS = config.get(benchmark_section_name, 'graphs')


if __name__ == "__main__":
    main()