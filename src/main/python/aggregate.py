import collections
import csv
import sys
import os


NAME_EVAL = 'eval-results.csv'
SCALE_POINTS = {
    'binary' : 2,
    '5star' : 5,
    '5halfstar' : 9,
}

def main(paths):
    results = set()
    for path in paths:
        results.update(find_files(path))

    fields = []
    for path in results:
        (inScale, n) = get_info(path)
        means = get_field_means(path)
        if not fields:
            fields = list(means.keys())
            fields.sort()
            fields = ['MI-native', 'MI-native.ByUser'] + fields
            print ','.join(['n', 'inscale'] + fields)
        means['MI-native'] = means['MI-' + str(inScale)]
        means['MI-native.ByUser'] = means['MI-' + str(inScale) + '.ByUser']

        tokens = [n, inScale]
        for f in fields:
            tokens.append(means.get(f, 0.0))

        print ','.join(map(str, tokens))

def get_info(path):
    """ Path format is ./splits/ml-100k/5star-2/eval-results.csv """
    values = {}
    last_directory = os.path.split(os.path.split(path)[0])[1]
    (inScale, n) = last_directory.split('-')
    return SCALE_POINTS[inScale], int(n)

def get_field_means(path):
    reader = csv.DictReader(open(path))
    values = collections.defaultdict(list)
    for record in reader:
        for (field, value) in record.items():
            try:
                v = float(value)
                if field.startswith('MI-') and field.endswith('.ByUser'):
                    key = field[len('MI-'):-len('.ByUser')]
                    field = 'MI-' + str(SCALE_POINTS[key]) + '.ByUser'
                elif field.startswith('MI-'):
                    key = field[len('MI-'):]
                    field = 'MI-' + str(SCALE_POINTS[key])
                values[field].append(v)
            except ValueError:
                pass
    for (field, field_values) in values.items():
        values[field] = 1.0 * sum(field_values) / len(field_values)

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
