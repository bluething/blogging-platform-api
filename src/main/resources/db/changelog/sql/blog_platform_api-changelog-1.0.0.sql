-- changeset habib.machpud:create-table-categories
CREATE TABLE categories (
  id CHAR(26) PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
);
-- changeset habib.machpud:create-table-tags
CREATE TABLE tags (
  id CHAR(26) PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);
-- changeset habib.machpud:create-table-posts
CREATE TABLE posts (
  id CHAR(26) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  category_id CHAR(26) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_posts_category FOREIGN KEY (category_id)
    REFERENCES categories(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
);
-- changeset habib.machpud:create-table-post_tags
CREATE TABLE post_tags (
  post_id CHAR(26) NOT NULL,
  tag_id CHAR(26) NOT NULL,
  PRIMARY KEY (post_id, tag_id),
  CONSTRAINT fk_posttags_post FOREIGN KEY (post_id)
    REFERENCES posts(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_posttags_tag FOREIGN KEY (tag_id)
    REFERENCES tags(id)
    ON DELETE CASCADE
);