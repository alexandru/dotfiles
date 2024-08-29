;;; init.el

;;; Commentary:
;; Alex's .emacs file, use at you own risk
;; --------------------------------------------

;;; Code:

(add-to-list 'load-path "~/.config/emacs/elpa/")
(add-to-list 'load-path "~/.config/emacs/settings/")

(require 'package-manager-config)
(require 'my-functions)
(require 'base-settings)
(require 'global-keymap)
(require 'fira-code-config)
(require 'yasnippet-config)
(require 'paredit-config)
(require 'markdown-config)
(require 'deft-config)
(require 'projectile-config)
;;(require 'evil-config)
