#!/usr/bin/env python3
import argparse
import configparser
import subprocess


def main():
    parser = argparse.ArgumentParser(description='Run layout benchmark')
    parser.add_argument('config', type=str, help='path to config')

    args = parser.parse_args()
    config_path = args.config

    config = configparser.ConfigParser()
    config.read(config_path)

    params = build_params(config)
    print('Running command:\n================\n\n' + ' '.join(params))

    repeat = config.getint('SYSTEM', 'repeat')
    for i in range(repeat):
        print('\n+++ REPEAT: ' + str(i + 1) + '/' + str(repeat) + ' +++')
        subprocess.check_call(params)


def check_section(config, section_name):
    if config.has_section(section_name) is False:
        raise RuntimeError('Error while reading config: section "' + section_name + '" not found.')


def build_params(config):
    params = []

    # read system config
    system_section_name = 'SYSTEM'
    check_section(config, system_section_name)

    if config.has_option(system_section_name, 'flink'):
        flink = config.get(system_section_name, 'flink')
        params.append(flink + "/bin/flink")
        params.append("run")
    else:
        raise RuntimeError("Error while reading config: missing flink executable.")

    if config.has_option(system_section_name, 'hadoop'):
        pass

    # read flink config
    flink_section_name = 'FLINK'
    check_section(config, flink_section_name)

    if config.has_option(flink_section_name, 'parallelism'):
        parallelism = config.get(flink_section_name, 'parallelism')
        params.append("-p")
        params.append(parallelism)
    else:
        raise RuntimeError("Error while reading config: missing parallelism.")

    if config.has_option(flink_section_name, 'class'):
        clazz = config.get(flink_section_name, 'class')
        params.append("-c")
        params.append(clazz)
    else:
        raise RuntimeError("Error while reading config: missing benchmark class.")

    if config.has_option(flink_section_name, 'jar'):
        jar = config.get(flink_section_name, 'jar')
        params.append(jar)
    else:
        raise RuntimeError("Error while reading config: missing jar.")

    # read benchmark config
    benchmark_section_name = 'BENCHMARK'
    check_section(config, benchmark_section_name)

    if config.has_option(benchmark_section_name, 'inputpath'):
        input_path = config.get(benchmark_section_name, 'inputpath')
        params.append('-input')
        params.append(input_path)

    if config.has_option(benchmark_section_name, 'outputpath'):
        output_path = config.get(benchmark_section_name, 'outputpath')
        params.append('-output')
        params.append(output_path)

    if config.has_option(benchmark_section_name, 'statisticspath'):
        statistics_path = config.get(benchmark_section_name, 'statisticspath')
        params.append('-statistics')
        params.append(statistics_path)

    if config.has_option(benchmark_section_name, 'width'):
        width = config.get(benchmark_section_name, 'width')
        params.append('-width')
        params.append(width)

    if config.has_option(benchmark_section_name, 'height'):
        height = config.get(benchmark_section_name, 'height')
        params.append('-height')
        params.append(height)

    if config.has_option(benchmark_section_name, 'vertices'):
        vertices = config.get(benchmark_section_name, 'vertices')
        params.append('-vertices')
        params.append(vertices)

    if config.has_option(benchmark_section_name, 'sgf'):
        sgf = config.get(benchmark_section_name, 'sgf')
        params.append('-sgf')
        params.append(sgf)

    if config.has_option(benchmark_section_name, 'dgf'):
        dgf = config.get(benchmark_section_name, 'dgf')
        params.append('-dgf')
        params.append(dgf)

    if config.has_option(benchmark_section_name, 'iterations'):
        iterations = config.get(benchmark_section_name, 'iterations')
        params.append('-iterations')
        params.append(iterations)

    if config.has_option(benchmark_section_name, 'prelayoutiterations'):
        pre_layout_iterations = config.get(benchmark_section_name, 'prelayoutiterations')
        params.append('-prelayoutiterations')
        params.append(pre_layout_iterations)

    return params


if __name__ == "__main__":
    main()
