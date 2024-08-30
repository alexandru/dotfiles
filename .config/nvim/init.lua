if vim.g.vscode == nil then
  -- bootstrap lazy.nvim, LazyVim and your plugins
  require("config.lazy")
else
  -- configuration for vscode-neovim
  --
  -- disable compatibility to old-time vi
  vim.o.compatible = false
  -- highlight search results
  vim.o.hlsearch = true
  -- ignore case when searching
  vim.o.ignorecase = true
  -- incremental search
  vim.o.incsearch = true
end
