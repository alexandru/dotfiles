# shellcheck shell=bash

# ------------------------------------------------------------------------------
# Enabling profiling (when needed)
# ------------------------------------------------------------------------------

PROFILE_STARTUP=false
if [[ "$PROFILE_STARTUP" == true ]]; then
  zmodload zsh/zprof
fi

# ------------------------------------------------------------------------------
# Shell options
# ------------------------------------------------------------------------------

# Colorize ls output
export CLICOLOR=1

# Language, needed for scripts (e.g. Perl, etc)
export LANGUAGE=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8
export LC_TYPE=en_US.UTF-8

# History size
export HISTFILESIZE=100000
export HISTSIZE=100000
export SAVEHIST=100000
export HISTFILE=${ZDOTDIR:-$HOME}/.zsh_history
setopt INC_APPEND_HISTORY

# Allow comments in commands
setopt INTERACTIVE_COMMENTS

# Default keybindings (vim or emacs)
bindkey -e # emacs
# https://stackoverflow.com/questions/12382499/looking-for-altleftarrowkey-solution-in-zsh
bindkey '[C' forward-word
bindkey '[D' backward-word

# Adding Emacs to PATH
export PATH="$PATH:~/Applications/Emacs.app/Contents/MacOS/bin:/Applications/Emacs.app/Contents/MacOS/bin:~/Applications/Emacs.app/Contents/MacOS:/Applications/Emacs.app/Contents/MacOS"
# Adding IntelliJ IDEA to PATH
export PATH="$PATH:$HOME/Library/Application\ Support/JetBrains/Toolbox/scripts"
# Adding Cabal stuff
export PATH="$HOME/Library/Haskell/bin:$PATH"
# Adding user executables
export PATH="$HOME/bin:$HOME/.local/bin:/usr/local/sbin:$PATH"
# Adding Android tools
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
# Adding RClone installed via cask
if [ -d "/usr/local/opt/rclone-mac" ]; then
  export PATH="/usr/local/opt/rclone-mac/libexec/rclone:$PATH"
fi

# Replaces vim with neovim
if command -v nvim 1>/dev/null 2>&1; then
  alias vi='nvim'
  alias vim='nvim'
  alias vimdiff='nvim -d'
else
  echo "WARN: nvim not installed!" >&2
fi

# Default editor
export EDITOR="nvim"
export VISUAL="$EDITOR"
export ALTERNATE_EDITOR="vim"

# Terminal colors (ls)
export CLICOLOR=1
export LSCOLORS=GxFxCxDxBxegedabagaced

# Disabling .NET Core telemetry
export DOTNET_CLI_TELEMETRY_OPTOUT="1"

# Make iTerm2 put the current directory in the title
precmd() {
  echo -ne "\e]1;$(print -rD "$PWD")\a"
}

# Enable compatibility for Bash completion definitions
autoload -U +X compinit && compinit
autoload -U +X bashcompinit && bashcompinit
# Enable hooks for triggering stuff when changing directories
autoload -U add-zsh-hook

# https://github.com/direnv/direnv/
eval "$(direnv hook zsh)"

# Loading secrets (must be generated by ./setup.sh)
if [ -f "$HOME/.secrets" ]; then
  source "$HOME/.secrets"
else
  echo "WARN: $HOME/.secrets not found!" >&2
fi

# ------------------------------------------------------------------------------
# ALIASES
# ------------------------------------------------------------------------------

# My dot files configuration
alias config="\"$(which git)\" --git-dir=\"$HOME/.dotfiles/\" --work-tree=\"$HOME\""

## Editor aliases
alias e="nvim"
alias ew="neovide --fork"
alias notes='cd "$HOME/Notes/" && neovide .'

## Util aliases
alias ts="date +%s"

git_refresh() {
  local remote="${1:-origin}"
  local branch="${2:-$(git branch -q --no-color | cut -d' ' -f 2)}"
  CMD="git fetch \"$remote\" && git merge --ff-only \"$remote/$branch\""
  echo "$CMD" && eval "$CMD"
}

# ------------------------------------------------------------------------------
# Customize PROMPT
#
# https://scriptingosx.com/2019/07/moving-to-zsh-06-customizing-the-zsh-prompt/
# https://jonasjacek.github.io/colors/
# https://www.calmar.ws/vim/256-xterm-24bit-rgb-color-chart.html
#
# ------------------------------------------------------------------------------

autoload -Uz vcs_info
precmd_vcs_info() {
  vcs_info
}
precmd_functions+=(precmd_vcs_info)
setopt prompt_subst

export PROMPT="%F{196}%B%(?..?%? )%b%f%F{27}%2~%f%B\$vcs_info_msg_0_%f%b %F{245}%#%f "
# export RPROMPT="%B\$vcs_info_msg_0_%f%b"

zstyle ':vcs_info:git:*' formats '%F{31}  %b%f'
zstyle ':vcs_info:*' enable git

# ------------------------------------------------------------------------------
# Ruby Setup
# ------------------------------------------------------------------------------

if [[ -d "/usr/local/opt/ruby/bin" ]]; then
    export PATH="/usr/local/opt/ruby/bin:$PATH"
elif [[ -d "/opt/homebrew/opt/ruby/bin" ]]; then
    export PATH="/opt/homebrew/opt/ruby/bin:$PATH"
fi

if command -v rbenv 1>/dev/null 2>&1; then
  ## Loads rbenv
  eval "$(rbenv init -)"
else
  echo "WARN: rbenv is not installed!" >&2
fi

# ------------------------------------------------------------------------------
# Python Setup
# ------------------------------------------------------------------------------

export PYENV_ROOT="$HOME/.pyenv"
export PATH="$PYENV_ROOT/bin:$PATH"

if command -v pyenv 1>/dev/null 2>&1; then
  eval "$(pyenv init -)"
  eval "$(pyenv virtualenv-init -)"
  # export PATH="$(pyenv root)/shims:$PATH"
else
  echo "WARN: pyenv is not installed!" >&2
fi

# ------------------------------------------------------------------------------
# Node.js Setup
# ------------------------------------------------------------------------------

# Make local modules take precedence over global modules
# export PATH=./node_modules/.bin:$PATH
# export NVM_DIR="$HOME/.nvm"

# load-nvmrc() {
#     local node_version="$(nvm version)"
#     local nvmrc_path="$(nvm_find_nvmrc)"

#     if [ -n "$nvmrc_path" ]; then
#         local nvmrc_node_version=$(nvm version "$(cat "${nvmrc_path}")")

#         if [ "$nvmrc_node_version" = "N/A" ]; then
#             nvm install
#         elif [ "$nvmrc_node_version" != "$node_version" ]; then
#             nvm use
#         fi
#     fi
# }

# if [ -s "/usr/local/opt/nvm/nvm.sh" ]; then
#     # shellcheck source=/dev/null
#     \. "/usr/local/opt/nvm/nvm.sh" # This loads nvm
#     # shellcheck source=/dev/null
#     [ -s "/usr/local/opt/nvm/etc/bash_completion.d/nvm" ] && \. "/usr/local/opt/nvm/etc/bash_completion.d/nvm"  # This loads nvm bash_completio

#     add-zsh-hook chpwd load-nvmrc
#     load-nvmrc
# else
#     echo "WARN: nvm is not installed!" >&2
# fi

# ------------------------------------------------------------------------------
# Rust
# ------------------------------------------------------------------------------

export PATH="$HOME/.cargo/bin:$PATH"

# ------------------------------------------------------------------------------
# Haskell
# ------------------------------------------------------------------------------

if command -v stack 1>/dev/null 2>&1; then
  eval "$(stack --bash-completion-script stack)"
else
  echo "WARN: Haskell's stack not installed" >&2
fi

# ------------------------------------------------------------------------------
# Scala
# ------------------------------------------------------------------------------

if command -v coursier 1>/dev/null 2>&1; then
  export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
else
  echo "WARN: Scala's coursier not installed" >&2
fi

# ------------------------------------------------------------------------------
# Java
# ------------------------------------------------------------------------------

export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

alias gw=./gradlew

# ------------------------------------------------------------------------------
# Nix
# ------------------------------------------------------------------------------

alias nix-env-search="nix-env -qaP"
alias nix-env-install="nix-env -iA"
alias nix-env-update-all="nix-channel --update nixpkgs && nix-env -u '*'"
alias nix-up="nix-env -u"
alias nix-gc="nix-collect-garbage -d"

# ------------------------------------------------------------------------------
# Proxy utils
# ------------------------------------------------------------------------------

proxy_enable() {
  export HTTP_PROXY="http://localhost:3128"
  export http_proxy="$HTTP_PROXY"
  export HTTPS_PROXY="$HTTP_PROXY"
  export https_proxy="$HTTP_PROXY"
  echo "HTTPS proxy is active: $HTTPS_PROXY"
}

proxy_disable() {
  unset HTTP_PROXY
  unset http_proxy
  unset HTTPS_PROXY
  unset https_proxy
}

# Enable by default
# proxy_enable()

# ------------------------------------------------------------------------------
# 1Password
# ------------------------------------------------------------------------------

export SSH_AUTH_SOCK=~/Library/Group\ Containers/2BUA8C4S2C.com.1password/t/agent.sock

# ------------------------------------------------------------------------------
# Homebrew
# ------------------------------------------------------------------------------

export HOMEBREW_CASK_OPTS="--appdir=~/Applications"

# ------------------------------------------------------------------------------
# iTerm2 shell intergration
# ------------------------------------------------------------------------------

test -e "${HOME}/.iterm2_shell_integration.zsh" && source "${HOME}/.iterm2_shell_integration.zsh"

# ------------------------------------------------------------------------------
# .NET / C# / F#
# ------------------------------------------------------------------------------

# Add .NET Core SDK tools
export PATH="$PATH:$HOME/.dotnet/tools"

# ------------------------------------------------------------------------------
# OCaml
# ------------------------------------------------------------------------------

if command -v opam 1>/dev/null 2>&1; then
    eval $(opam env --switch=default)
else
    echo "WARN: opam not installed!" >&2
fi

# ------------------------------------------------------------------------------
# END OF FILE
# ------------------------------------------------------------------------------

if [[ "$PROFILE_STARTUP" == true ]]; then
  zprof
fi

