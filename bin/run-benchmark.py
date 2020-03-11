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

    # read system config
    system_config = config['SYSTEM']
    flink = system_config['flinkroot']
    hdfs = system_config['hdfsroot']

    # read flink config
    flink_config = config['FLINK']
    clazz = flink_config['class']
    jar = flink_config['jar']
    parallelism = flink_config['parallelism']
    repeat = flink_config['repeat']

    # read benchmark config
    benchmark_config = config['BENCHMARK']
    input_path = benchmark_config['inputpath']
    output_path = benchmark_config['outputpath']
    statistics_path = benchmark_config['statisticspath']
    width = benchmark_config['width']
    height = benchmark_config['height']
    vertices = benchmark_config['vertices']
    sgf = benchmark_config['sgf']
    dgf = benchmark_config['dgf']
    iterations = benchmark_config['iterations']

    # build subprocess call
    flink_param = flink + "/bin/flink run "
    parallelism_param = "-p " + parallelism + " "
    class_param = "-c " + clazz + " "
    jar_param = jar + " "
    input_param = "-input " + input_path + " "
    output_param = "-output " + output_path + " "
    statistics_param = "-statistics " + statistics_path + " "
    width_param = "-width " + width + " "
    height_param = "-height " + height + " "
    vertices_param = "-vertices " + vertices + " "
    sgf_param = "-sgf " + sgf + " "
    dgf_param = "-dgf " + dgf + " "

    subprocess.run([
        flink_param,
        parallelism_param,
        class_param,
        jar_param,
        input_param,
        output_param,
        statistics_param,
        width_param,
        height_param,
        vertices_param,
        sgf_param,
        dgf_param
    ], shell=True, check=True)


if __name__ == "__main__":
    main()
