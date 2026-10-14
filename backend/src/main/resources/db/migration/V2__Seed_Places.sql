-- Migration V2: Seed default tourism categories, sources and full local places dataset for Mérida

INSERT INTO categories (name, description) VALUES
('Historical Monument', 'Monumentos históricos de Mérida'),
('Tourism Attraction', 'Atracciones turísticas principales'),
('Religious Site', 'Sitios de interés religioso e histórico'),
('Gastronomía', 'Restaurantes y bares de gastronomía típica'),
('Tiendas', 'Comercio local y artesanía'),
('monumento', 'Monumentos históricos y yacimientos romanos'),
('iglesia', 'Iglesias, basílicas y templos religiosos'),
('museo', 'Museos y centros de interpretación'),
('restaurante', 'Restaurantes de comida tradicional y de autor'),
('cafeteria', 'Cafeterías y desayunos de especialidad'),
('bar', 'Bares de tapas y cervecerías'),
('heladeria', 'Heladerías artesanales'),
('tienda_alimentos', 'Tiendas de productos alimenticios locales y artesanos'),
('souvenir', 'Tiendas de regalos y artesanía'),
('supermercado', 'Supermercados y alimentación'),
('libreria', 'Librerías y cómics');

INSERT INTO sources (code, name, description) VALUES
('SEED_DATA', 'Datos Iniciales Mérida', 'Datos semilla predeterminados para la guía turística de Mérida');

-- 1. Monumentos Históricos
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Teatro Romano de Mérida', 'Construido entre los años 16 y 15 a.C., es uno de los monumentos más emblemáticos de la ciudad y sede del Festival Internacional de Teatro Clásico.', 'Plaza Margarita Xirgu, s/n, 06800 Mérida', 38.915222, -6.338278, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Anfiteatro Romano de Mérida', 'Inaugurado en el año 8 a.C., albergaba juegos gladiatorios y combates con fieras con capacidad para 15.000 espectadores.', 'Plaza Margarita Xirgu, s/n, 06800 Mérida', 38.915833, -6.3375, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Circo Romano', 'Antiguo recinto para carreras de carros construido a principios del siglo I d.C.', 'Av. Juan Carlos I, s/n', 38.9201, -6.3315, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Acueducto de los Milagros', 'Impresionante obra de ingeniería romana destinada a traer agua a la ciudad desde el embalse de Proserpina.', 'Av. de los Milagros, 06800 Mérida', 38.923889, -6.348611, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Templo de Diana', 'Templo de culto imperial situado en el foro municipal de la antigua Emerita Augusta.', 'Calle Romero Leal, 06800 Mérida', 38.916944, -6.344444, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Arco de Trajano', 'Puerta de acceso con arco monumental romano situado en el centro histórico.', 'C/ Trajano, s/n', 38.917817, -6.346423, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Puente Romano sobre el Guadiana', 'Uno de los puentes romanos más largos de la antigüedad que se conservan en el mundo.', 'Plaza de España, 06800 Mérida', 38.913889, -6.347778, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Alcazaba Árabe de Mérida', 'Fortificación musulmana construida por Abderramán II en el año 835 para controlar el puente romano.', 'Paseo de Roma, 06800 Mérida', 38.914167, -6.346389, id FROM categories WHERE name = 'monumento';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Casa del Mitreo', 'Lujosa vivienda de la Mérida romana, construida en torno al siglo I a.C.', 'Calle Oviedo, s/n', 38.911180, -6.341099, id FROM categories WHERE name = 'monumento';

-- 2. Museos
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Museo Nacional de Arte Romano', 'Obra del arquitecto Rafael Moneo, alberga la mejor colección de escultura, mosaicos y piezas de la Augusta Emerita romana.', 'Calle José Ramón Mélida, s/n', 38.916984, -6.339626, id FROM categories WHERE name = 'museo';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Museo de Arte Visigodo', 'Ubicado en la antigua Iglesia de Santa Clara, contiene una colección excepcional de escultura y piezas del periodo visigodo.', 'Calle Santa Julia, 1', 38.917151, -6.346681, id FROM categories WHERE name = 'museo';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Museo Abierto de Mérida (MAM)', 'Centro de recepción de visitantes con el espacio expositivo Los Placeres de Augusta Emerita.', 'Calle Cabo Verde, s/n', 38.918752, -6.336298, id FROM categories WHERE name = 'museo';

-- 3. Iglesias y Templos
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Basílica de Santa Eulalia y Hornito', 'Basílica martirial considerada núcleo fundamental del nacimiento del cristianismo en la Península Ibérica.', 'Avenida de Extremadura, s/n', 38.920667, -6.342030, id FROM categories WHERE name = 'iglesia';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Concatedral de Santa María de la Asunción', 'Templo católico construido entre los siglos XIII y XV sobre la antigua catedral visigoda de San Jerusalem.', 'Plaza de España, 06800 Mérida', 38.916389, -6.343611, id FROM categories WHERE name = 'iglesia';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Iglesia de Ntra. Sra. del Carmen', 'Edificio adyacente del barroco desornamentado con camarín histórico.', 'Calle Almendralejo, s/n', 38.919146, -6.346479, id FROM categories WHERE name = 'iglesia';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Ermita Nuestra Señora de la Antigua', 'Ermita de finales del siglo XV de estilo gótico con portada renacentista.', 'Avenida Juan Carlos I, s/n', 38.924282, -6.327704, id FROM categories WHERE name = 'iglesia';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Convento de las Concepcionistas', 'Convento fundado en 1588 en el centro histórico.', 'Calle Concepción, 5', 38.917923, -6.345546, id FROM categories WHERE name = 'iglesia';

-- 4. Librerías
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Emérita Cómics', 'Punto de encuentro para la comunidad amante del cómic, juegos de mesa y literatura gráfica.', 'Calle Berzocana, 3', 38.916763, -6.343872, id FROM categories WHERE name = 'libreria';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Librería Mercader de Sedas', 'Librería especializada en narrativa, novedades editoriales y literatura infantil.', 'Calle Santa Eulalia, 34', 38.917450, -6.343510, id FROM categories WHERE name = 'libreria';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Librería Solís', 'Librería de referencia en Mérida con amplio catálogo de libros y literatura.', 'Calle Camilo José Cela, 8', 38.918120, -6.342890, id FROM categories WHERE name = 'libreria';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Librería Dinos', 'Librería y papelería tradicional con atención personalizada.', 'Calle Santa Eulalia, 68', 38.918910, -6.342100, id FROM categories WHERE name = 'libreria';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Librería Papelería San Francisco', 'Librería local con gran selección de literatura y novedades.', 'Calle San Francisco, 22', 38.918650, -6.345210, id FROM categories WHERE name = 'libreria';

-- 5. Tiendas de Alimentos Locales
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'OVAL a tu gusto', 'Tu tienda de productos Típicos Extremeños, orgullosos de llevar el sabor de Extremadura a cada rincón.', 'Calle San Francisco, 16', 38.918418, -6.345401, id FROM categories WHERE name = 'tienda_alimentos';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Feliciano Becerra Jiménez', 'Especialistas en jamones y productos extremeños de alta calidad.', 'Calle José Ramón Mélida, 56', 38.916702, -6.339701, id FROM categories WHERE name = 'tienda_alimentos';

-- 6. Souvenirs y Regalos
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Memoria Mérida', 'Tienda de souvenirs y regalos inspirados en la historia y cultura de Mérida.', 'C. José Ramón Mélida, 18', 38.917675, -6.342003, id FROM categories WHERE name = 'souvenir';

-- 7. Supermercados
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Supermercado Spar', 'Supermercado con productos locales y alimentación.', 'Calle José Ramón Mélida, 21', 38.917621, -6.341394, id FROM categories WHERE name = 'supermercado';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Supermercado Covirán', 'Alimentación y productos de proximidad.', 'Calle Sagasta, 4', 38.916343, -6.343701, id FROM categories WHERE name = 'supermercado';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Supermercado Día', 'Cadena de supermercados de alimentación.', 'Calle Marquesa de Pinares, 1', 38.920152, -6.344130, id FROM categories WHERE name = 'supermercado';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Carrefour Express', 'Supermercado urbano en el centro de Mérida.', 'Calle San Salvador, 1', 38.916498, -6.347812, id FROM categories WHERE name = 'supermercado';

-- 8. Restaurantes y Cafeterías
INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'La Tahona', 'Restaurante pintoresco con paredes de piedra que sirve tapas y platos tradicionales.', 'Calle Alvarado, 5', 38.917813, -6.347139, id FROM categories WHERE name = 'restaurante';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'A de Arco', 'Comida de calidad en un entorno histórico junto al Arco de Trajano.', 'Calle Trajano, 8', 38.917892, -6.346380, id FROM categories WHERE name = 'restaurante';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Agallas Gastro&Food', 'Experiencia gastronómica de descubrimiento en el corazón de Mérida.', 'Calle Suárez Somonte, 2', 38.913956, -6.344170, id FROM categories WHERE name = 'restaurante';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'EL BAR Old School Food', 'El restaurante que quería ser Bar con cocina de autor.', 'Calle San Salvador, 4', 38.917143, -6.348368, id FROM categories WHERE name = 'restaurante';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'La Carbonería', 'Las mejores carnes y pescados a la brasa con el toque inconfundible del fuego.', 'Plaza de la Constitución, 4', 38.918631, -6.346243, id FROM categories WHERE name = 'restaurante';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Floco Original Coffee', 'Cafetería de especialidad con cafés premium y repostería artesanal.', 'Calle Delgado Valencia, 6', 38.917894, -6.343161, id FROM categories WHERE name = 'cafeteria';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Bar Volterra', 'Desayunos, tapas elaboradas y raciones en pleno centro.', 'Calle San Francisco, 10', 38.917706, -6.344661, id FROM categories WHERE name = 'bar';

INSERT INTO places (name, description, address, latitude, longitude, category_id)
SELECT 'Heladería Maria Limones', 'Helados artesanos y granizados con sabores tradicionales y novedosos.', 'Calle Santa Eulalia, 1B', 38.916366, -6.345667, id FROM categories WHERE name = 'heladeria';

-- 9. Asociar fuentes de datos
INSERT INTO place_sources (place_id, source_id, external_id, last_sync)
SELECT p.id, s.id, 'SEED/' || p.id, CURRENT_TIMESTAMP
FROM places p, sources s
WHERE s.code = 'SEED_DATA';
