;;; paredit-config.el --- structured editing of s-expressions -*- lexical-binding: t; -*-

;;; Commentary:
;;
;; https://wikemacs.org/wiki/Paredit-mode
;;

;;; Code:
(require 'package-manager-config)

(use-package paredit
  :ensure t
  :init
  (add-hook 'emacs-lisp-mode-hook #'paredit-mode)
  (add-hook 'paredit-mode-hook
            (lambda ()
              (define-key paredit-mode-map (kbd "C-s-<left>") 'paredit-forward-barf-sexp)
              (define-key paredit-mode-map (kbd "C-s-<right>") 'paredit-forward-slurp-sexp)
              (define-key paredit-mode-map (kbd "s-/") 'paredit-comment-dwim))))

(provide 'paredit-config)
;;; paredit-config.el ends here
