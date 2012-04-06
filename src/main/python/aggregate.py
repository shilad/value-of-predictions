import collections
import csv
import sys
import os


NAME_EVAL = 'eval-results.csv'

def main(paths):
    results = set()
    for path in paths:
        results.update(find_files(path))

    print ','.join(['alg', 'in-scale', 'num-ratings', 'field', 'value'])

    fields = []
    for path in results:
        (inScale, numRatings) = get_info(path)
        means = get_algorithm_field_means(path)

        for (alg, alg_means) in means.items():

            # add in the "native" measure"
            alg_means['MI-native'] = alg_means['MI-' + str(inScale)]
            alg_means['MI-native-corrected'] = alg_means['MI-' + str(inScale) +'-corrected']
            alg_means['MI-native.ByUser'] = alg_means['MI-' + str(inScale) + '.ByUser']

            for (field, value) in alg_means.items():
                    print ','.join([alg, str(inScale), str(numRatings), field, str(value)])


def get_info(path):
    """ Path format is ./splits/ml-100k/5-1000/eval-results.csv """
    values = {}
    last_directory = os.path.split(os.path.split(path)[0])[1]
    (inScale, numRatings) = last_directory.split('-')
    return int(inScale), int(numRatings)

def get_algorithm_field_means(path):
    reader = csv.DictReader(open(path))
    values = collections.defaultdict(lambda: collections.defaultdict(list))

    for record in reader:
        alg = record['Algorithm']
        for (field, value) in record.items():
            try:
                v = float(value)
                values[alg][field].append(v)
            except ValueError:
                pass

    for (alg, alg_values) in values.items():
        for (field, field_values) in alg_values.items():
            alg_values[field] = 1.0 * sum(field_values) / len(field_values)

    return values

def find_files(path):
    if path.endswith(NAME_EVAL):
        return set([path])
    elif os.path.isdir(path):
        result = set()
        for p in os.listdir(path):
            child = os.path.join(path, p)
            result.update(find_files(child))
        return result
    else:
        return set()


if __name__ == '__main__':
    main(sys.argv[1:])
