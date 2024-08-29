;;; projectile-config.el --- -*- lexical-binding: t; -*-

;;; Commentary:
;;
;;  - `https://github.com/bbatsov/projectile'
;;

;;; Code:
(require 'package-manager-config)

(use-package projectile
  :ensure t
  :config

  (projectile-mode +1)
  ;; Recommended keymap prefix on macOS
  (define-key projectile-mode-map (kbd "s-p") 'projectile-command-map)
  ;; Recommended keymap prefix on Windows/Linux
  (define-key projectile-mode-map (kbd "C-c p") 'projectile-command-map)  
)

(provide 'projectile-config)
;;; projectile-config.el ends here
