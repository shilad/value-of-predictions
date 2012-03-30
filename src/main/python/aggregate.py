import csv
import sys
import os


NAME_EVAL = 'eval-results.csv'
SCALE_POINTS = {
    'binary' : 2,
    '5star' : 5,
    '5halfstar' : 9,
}

def main(field, paths):
    results = set()
    for path in paths:
        results.update(find_files(path))

    print 'in-scale,out-scale,num-ratings,%s' % field
    for path in results:
        (inScale, outScale, n) = get_info(path)
        mean = get_field_mean(field, path)
        if inScale == 9: continue # remove me!
        print ','.join(map(str, [inScale, outScale, n, mean]))

def get_info(path):
    """ Path format is ./splits/ml-100k/5star-to-5star-2/eval-results.csv """
    last_directory = os.path.split(os.path.split(path)[0])[1]
    (inScale, _, outScale, n) = last_directory.split('-')
    return SCALE_POINTS[inScale], SCALE_POINTS[outScale], int(n)

def get_field_mean(field, path):
    reader = csv.DictReader(open(path))
    values = []
    for record in reader:
        values.append(float(record[field]))
    if values:
        return 1.0 * sum(values) / len(values)
    else:
        return 0.0

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
    main(sys.argv[1], sys.argv[2:])
