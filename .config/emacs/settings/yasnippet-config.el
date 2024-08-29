;;; yasnippet-config.el --- -*- lexical-binding: t; -*-

;;; Commentary:
;;
;; `https://github.com/joaotavora/yasnippet'
;;

;;; Code:
(require 'package-manager-config)

(use-package yasnippet
  :ensure t
  :config
  (yas-global-mode 1))

(use-package yasnippet-snippets
  :ensure t
  :after yasnippet)

(provide 'yasnippet-config)
;;; yasnippet-config.el ends here
