UPDATE product_detail
SET content = REPLACE(REPLACE(content, '<html><body>', '<div>'), '</body></html>', '</div>');