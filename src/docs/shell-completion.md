# Shell completion

`CliCompletion` turns the same annotated classes the parser reads into completion scripts
for bash, fish and zsh.

```java
public static void main(final String[] args) {
    if (args.length == 1 && "--completion".equals(args[0])) {
        System.out.println(CliCompletion.toBash(Args.class));   // or toFish, toZsh
        return;
    }
    ...
}
```

## The API

Each shell has three overloads:

```java
CliCompletion.toBash(Args.class);                    // name from @CliCommand, or the class name
CliCompletion.toBash("mytool", Args.class);          // explicit name
CliCompletion.toBash(parser);                        // from a CliParserBuilder
```

The builder overload is the one that knows about [subcommands](subcommands.md): it takes
the builder's `name(...)`, and every registered subcommand with its own options.

The command name resolves as: builder `name(...)` → `@CliCommand.name` → the class name
lowercased.

## What goes into the script

* every option that is not `isHidden`, with its short name, long name and description,
* aliases, as additional entries,
* flags versus value-taking options: value-taking options complete their value (file
  names) instead of the option itself,
* positional arguments complete as files (bash and zsh),
* subcommands, with the options of the selected level.

## Installing the scripts

### bash

```bash
# one-off for the current shell
source <(mytool --completion)

# or install for every shell
mytool --completion > ~/.mytool-completion.bash
echo 'source ~/.mytool-completion.bash' >> ~/.bashrc

# system wide
mytool --completion > /etc/bash_completion.d/mytool
```

Generated script for a command named `backup`:

```bash
# bash completion for backup
backup_completions() {
    local cur command
    cur="${COMP_WORDS[COMP_CWORD]}"
    if [[ $cur == -* ]]; then COMPREPLY=( $(compgen -W "-d --directory -v --verbose --include -q --quiet -h --help" -- "$cur") ); else COMPREPLY=( $(compgen -f -- "$cur") ); fi
}
complete -F backup_completions backup
```

With subcommands the script switches on the first bare word and completes the options of
that subcommand (subcommand names are offered at the root):

```bash
# bash completion for tool
tool_completions() {
    local cur command
    cur="${COMP_WORDS[COMP_CWORD]}"
    command=""
    for (( i=1; i < COMP_CWORD; i++ )); do
        if [[ "${COMP_WORDS[i]}" != -* && -z "$command" ]]; then command="${COMP_WORDS[i]}"; fi
    done
    case "$command" in
        add)
            if [[ $cur == -* ]]; then COMPREPLY=( $(compgen -W "-f --force -h --help" -- "$cur") ); else COMPREPLY=( $(compgen -f -- "$cur") ); fi
            ;;
        commit)
            if [[ $cur == -* ]]; then COMPREPLY=( $(compgen -W "-h --help -v --verbose" -- "$cur") ); else COMPREPLY=( $(compgen -f -- "$cur") ); fi
            ;;
        *)
            if [[ $cur == -* ]]; then COMPREPLY=( $(compgen -W "-h --help -v --verbose add commit" -- "$cur") ); else COMPREPLY=( $(compgen -f -- "$cur") ); fi
            ;;
    esac
}
complete -F tool_completions tool
```

### fish

fish loads completions by file name, so drop the script in the completions directory:

```bash
mkdir -p ~/.config/fish/completions
mytool --completion > ~/.config/fish/completions/mytool.fish
```

```fish
complete -c backup -s d -l directory -r -F -d 'The directory to back up'
complete -c backup -s v -l verbose -d 'More output, repeat for more'
complete -c backup -l include -r -F -d 'Glob patterns to include'
complete -c backup -s q -l quiet -d 'No output'
complete -c backup -s h -l help -d 'Prints this help'
```

`-r -F` marks an option that takes a value and completes it with file names. For a command
with subcommands, fish gets `-a <name>` lines for the subcommands and a
`__fish_seen_subcommand_from` condition on their options.

### zsh

zsh looks the function up by file name in `fpath`, before `compinit` runs:

```bash
mkdir -p ~/.zfunc
mytool --completion > ~/.zfunc/_mytool
# in .zshrc, before compinit:
fpath=(~/.zfunc $fpath)
autoload -Uz compinit && compinit
```

```zsh
#compdef backup

backup() {
    local state
    _arguments \
        '-d[The directory to back up]:directory:_files' \
        '-v[More output, repeat for more]' \
        '--include[Glob patterns to include]:include:_files' \
        '-q[No output]' \
        '-h[Prints this help]' \
        '*::positional:_files'
}

backup "$@"
```

Descriptions come from the annotations. `_arguments` treats `[`, `]` and `'` as syntax, so
the generator strips them from descriptions.

## Notes

* The scripts are pure text: write them where your packaging expects, or generate them at
  install time.
* Value completion is file names for every value-taking option; there is no per-option
  word list.
* In the bash and zsh scripts the subcommand switch looks at the first bare word only,
  so options of nested sub-subcommands are not completed there; fish nests as deep as you
  register.
