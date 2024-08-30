-- Options are automatically loaded before lazy.nvim startup
-- Default options that are always set: https://github.com/LazyVim/LazyVim/blob/main/lua/lazyvim/config/options.lua
-- Add any additional options here

if vim.g.neovide then
  vim.g.neovide_theme = "auto"
  vim.opt.guifont = "FiraCode Nerd Font Mono:h18"
end

-- Fixes markdown rendering to not conceal syntax
vim.o.conceallevel = 0
