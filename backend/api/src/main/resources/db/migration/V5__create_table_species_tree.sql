CREATE SEQUENCE IF NOT EXISTS species_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS species_tree (
    id bigint PRIMARY KEY DEFAULT nextval('species_id_seq'),
    title text NOT NULL
);

INSERT INTO species_tree(title)
VALUES ('Ель'), ('Кедр'), ('Лиственница'), ('Пихта'), ('Сосна'), ('Кипарисовик'), ('Можжевельник'), ('Туя'), ('Акация'),
       ('Береза'), ('Барбарис'), ('Боярышник'), ('Вишня'), ('Вяз'), ('Груша'), ('Дерен'), ('Дуб'), ('Жимолость'), ('Ива'),
       ('Ирга'), ('Калина'), ('Клен'), ('Кизильник'), ('Липа'), ('Ольха'), ('Орех'), ('Осина'), ('Рябина'), ('Слива'),
       ('Сирень'), ('Спирея'), ('Тополь'), ('Черемуха'), ('Чубушник'), ('Яблоня'), ('Ясень');