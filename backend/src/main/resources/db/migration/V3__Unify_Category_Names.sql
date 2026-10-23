-- Migration V3: Unify category vocabulary in Spanish
-- The importer now normalizes OpenStreetMap tags to the same Spanish categories used by the seed
-- and the Android local data. Legacy English/aggregate categories are remapped and removed.

INSERT INTO categories (name, description)
SELECT 'ropa', 'Tiendas de ropa y calzado'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'ropa');

INSERT INTO categories (name, description)
SELECT 'tienda_general', 'Comercio general y otras tiendas'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'tienda_general');

UPDATE places SET category_id = (SELECT id FROM categories WHERE name = 'monumento')
WHERE category_id IN (SELECT id FROM categories WHERE name IN ('Historical Monument', 'Tourism Attraction', 'Monument'));

UPDATE places SET category_id = (SELECT id FROM categories WHERE name = 'iglesia')
WHERE category_id IN (SELECT id FROM categories WHERE name = 'Religious Site');

UPDATE places SET category_id = (SELECT id FROM categories WHERE name = 'restaurante')
WHERE category_id IN (SELECT id FROM categories WHERE name = 'Gastronomía');

UPDATE places SET category_id = (SELECT id FROM categories WHERE name = 'tienda_general')
WHERE category_id IN (SELECT id FROM categories WHERE name IN ('Tiendas', 'tiendasGenerales'));

DELETE FROM categories
WHERE name IN ('Historical Monument', 'Tourism Attraction', 'Monument', 'Religious Site', 'Gastronomía', 'Tiendas', 'tiendasGenerales');
