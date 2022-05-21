# reference-segmentator

*This Java program was developed as part of coursework carried out at [the Department of Theoretical and Applied Linguistics](tipl.philol.msu.ru), Faculty of Philology, Moscow State University, Russia.*

## What this does

This is a prototype Java program that allows you to parse a list of references (contained in a text file) into their components (authors, book/article title, publisher, etc.) written as a `.json` file.

As an example, this reference:

> Аркадьев П. М. О некоторых особенностях склонения в адыгских языках // Плунгян В. А. (отв. ред.). Язык. Константы. Переменные: Памяти Александра Евгеньевича Кибрика. СПб.: Алетейя, 2014. С. 552—563.

will be converted to this JSON object:

```
  {
    "pagination": "С. 552—563",
    "city": "СПб.",
    "year": "2014",
    "collection-editors": "Плунгян В. А. (отв. ред.)",
    "collection-title": "Язык. Константы. Переменные: Памяти Александра Евгеньевича Кибрика",
    "publishers": "Алетейя",
    "article-title": "О некоторых особенностях склонения в адыгских языках",
    "authors": "Аркадьев П. М."
  },
```

See the `examples` folder for more examples.

## Usage

1. Download `reference-segmentator.zip` from the `dist` folder.

2. Run `reference-segmentator.jar` on your input file with references:

```
java -jar reference-segmentator.jar **input.txt**
```

If the path to the input file has not been specified, examples from input-sample.txt will be processed.

You can also specify the path for the output `.json` file (default: `output.json`) with `-o output_path` or `--output output_path`.

### Full command-line syntax with all options

```
java -jar [ input_path ] [ [ -o | --output ] output_path ] [ [ -c | --config ] config_path ]
```

Arguments:

- `input_path` (optional): the path to the input file with references (default: `input-sample.txt`)
- `output_path` (optional): the path to the output `.json` file with the parsed references (default: `output.json`)
- `config_path` (optional): the path to the configuration file (default: `res/config.txt`)

## Configuration file

The program uses a configuration file (default location: `res/config.txt`) specifying the citation formats that can be processed. More information about changing the configuration can be found in [the full text of the coursework](docs/segmentator-full-text.pdf) and [the accompanying handout](docs/segmentator-handout.pdf) (both in Russian).
