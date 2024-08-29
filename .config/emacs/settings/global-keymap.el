;;; global-keymap.el --- My custom keybindings -*- lexical-binding: t; -*-

;;; Commentary:
;;

;;; Code:

;; Unset interesting keys combinations that were automatically
;; set for Mac OS
(global-unset-key (kbd "s-l"))
(global-unset-key (kbd "s-p"))

;; Opposite of \M-q (fill-paragraph)
(global-set-key "\M-Q" 'unfill-paragraph)

;; Editing improvements
(global-set-key [C-tab] 'completion-at-point)

;; Toggles soft line/word wrap (same behavior as in VS Code)
(global-set-key "\M-z" 'toggle-truncate-lines)

;; Recent files
(global-set-key "\C-x\ \C-r" 'recentf-open-files)
(global-set-key (kbd "s-e") 'recentf-open-files)


;; Shortcut for maximizing the window
(global-set-key (kbd "C-s-=") (lambda () (interactive) (set-frame-parameter nil 'fullscreen 'maximized)))

;;; Find file under cursor
(global-set-key (kbd "s-.") 'ffap)

;;; Inserts the em-dash
(global-set-key (kbd "M-_") (lambda () (interactive) (insert "–")))
(global-set-key (kbd "M--") (lambda () (interactive) (insert "—")))

;; Global shortcut for setting comments
(global-set-key (kbd "s-/") 'comment-dwim)

(provide 'global-keymap)
;;; global-keymap.el ends here

