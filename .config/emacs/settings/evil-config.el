;;; evil-config.el --- -*- lexical-binding: t; -*-

;;; Commentary:
;;
;; https://github.com/emacs-evil/evil
;;

;;; Code:
(require 'package-manager-config)

(use-package undo-fu
  :ensure t)

(use-package evil
  :ensure t
  :config
  (evil-mode 1)
  (evil-set-undo-system 'undo-fu))

(use-package vimrc-mode
  :ensure t
  :config
  (add-to-list 'auto-mode-alist '("\\.vim\\(rc\\)?\\'" . vimrc-mode)))

(provide 'evil-config)
;;; evil-config.el ends here
