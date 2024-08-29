# My Home directory (dotfiles)

Inspired by: <https://www.atlassian.com/git/tutorials/dotfiles>

## Setup

```bash
# Create alias in the current context
alias config="\"$(which git)\" --git-dir=\"$HOME/.dotfiles/\" --work-tree=\"$HOME\""

# Create empty repo
git clone --bare git@github.com:alexandru/dotfiles.git $HOME/.dotfiles

# Checkout with backup of existing files
mkdir -p .config-backup
config checkout
if [ $? = 0 ]; then
  echo "Checked out config.";
  else
    echo "Backing up pre-existing dot files.";
    config checkout 2>&1 | egrep "\s+\." | awk {'print $1'} | xargs -I{} mv {} .config-backup/{}
fi;
config checkout

# Ignore untracked files
config config status.showUntrackedFiles no
```

