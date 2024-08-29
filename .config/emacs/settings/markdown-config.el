;;; markdown-config.el --- -*- lexical-binding: t; -*-

;;; Commentary:
;;
;;  - `https://github.com/defunkt/markdown-mode'
;;  - `https://github.com/jrblevin/deft'
;;

;;; Code:

(require 'package-manager-config)

(use-package markdown-mode
  :ensure t
  :mode (("README\\.md\\'" . gfm-mode)
         ("\\.md\\'" . gfm-mode)
         ("\\.markdown\\'" . gfm-mode))  
  :config

  ;; https://emacs.stackexchange.com/questions/13189/github-flavored-markdown-mode-syntax-highlight-code-blocks/33497
  (setq markdown-fontify-code-blocks-natively t)
  (setq markdown-enable-wiki-links 1)
  
  (add-to-list 'auto-mode-alist
               '("\\.md.erb\\'" . markdown-mode)))


(provide 'markdown-config)
;;; markdown-config.el ends here
