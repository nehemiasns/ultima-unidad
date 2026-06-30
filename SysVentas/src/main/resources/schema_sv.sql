CREATE TABLE IF NOT EXISTS upeu_emisor (
                                           id_emisor IDENTITY NOT NULL,
                                           ruc VARCHAR(20) NOT NULL,
    nombre_comercial VARCHAR(60) NOT NULL,
    ubigeo VARCHAR(10) NOT NULL,
    domicilio_fiscal VARCHAR(60) NOT NULL,
    urbanizacion VARCHAR(40) NOT NULL,
    departamento VARCHAR(30) NOT NULL,
    provincia VARCHAR(30) NOT NULL,
    distrito VARCHAR(30) NOT NULL,
    CONSTRAINT upeu_emisor_pk PRIMARY KEY (id_emisor)
    );

CREATE TABLE IF NOT EXISTS upeu_perfil (
                                           id_perfil IDENTITY NOT NULL,
                                           nombre VARCHAR(20) NOT NULL,
    codigo VARCHAR(60) NOT NULL,
    CONSTRAINT upeu_perfil_pk PRIMARY KEY (id_perfil)
    );

CREATE TABLE IF NOT EXISTS upeu_proveedor (
                                              id_proveedor IDENTITY NOT NULL,
                                              dniruc VARCHAR(12) NOT NULL,
    nombres_raso VARCHAR(60) NOT NULL,
    tipo_doc VARCHAR(12) NOT NULL,
    celular VARCHAR(12) NOT NULL,
    email VARCHAR(40) NOT NULL,
    direccion VARCHAR(80) NOT NULL,
    CONSTRAINT upeu_proveedor_pk PRIMARY KEY (id_proveedor)
    );

CREATE TABLE IF NOT EXISTS upeu_unid_medida (
                                                id_unidad IDENTITY NOT NULL,
                                                nombre_medida VARCHAR(40) NOT NULL,
    CONSTRAINT upeu_unid_medida_pk PRIMARY KEY (id_unidad)
    );

CREATE TABLE IF NOT EXISTS upeu_usuario (
                                            id_usuario IDENTITY NOT NULL,
                                            usuario VARCHAR(20) NOT NULL,
    clave VARCHAR(60) NOT NULL,
    id_perfil INTEGER NOT NULL,
    estado VARCHAR(10) NOT NULL,
    CONSTRAINT upeu_usuario_pk PRIMARY KEY (id_usuario)
    );

CREATE TABLE IF NOT EXISTS upeu_comp_carrito (
                                                 id_compcarrito IDENTITY NOT NULL,
                                                 id_proveedor INTEGER NOT NULL,
                                                 id_producto INTEGER NOT NULL,
                                                 nombre_producto VARCHAR(40) NOT NULL,
    cantidad DOUBLE NOT NULL,
    punitario DOUBLE NOT NULL,
    ptotal DOUBLE NOT NULL,
    estado INTEGER NOT NULL,
    id_usuario INTEGER NOT NULL,
    CONSTRAINT upeu_comp_carrito_pk PRIMARY KEY (id_compcarrito)
    );

CREATE TABLE IF NOT EXISTS upeu_vent_carrito (
                                                 id_carrito IDENTITY NOT NULL,
                                                 dniruc VARCHAR(12) NOT NULL,
    id_producto INTEGER NOT NULL,
    nombre_producto VARCHAR(40) NOT NULL,
    cantidad DOUBLE NOT NULL,
    punitario DOUBLE NOT NULL,
    ptotal DOUBLE NOT NULL,
    estado INTEGER NOT NULL,
    id_usuario INTEGER NOT NULL,
    CONSTRAINT upeu_vent_carrito_pk PRIMARY KEY (id_carrito)
    );

CREATE TABLE IF NOT EXISTS upeu_compra (
                                           id_compra IDENTITY NOT NULL,
                                           precio_base DOUBLE NOT NULL,
                                           igv DOUBLE NOT NULL,
                                           preciototal DOUBLE NOT NULL,
                                           id_proveedor INTEGER NOT NULL,
                                           id_usuario INTEGER NOT NULL,
                                           serie VARCHAR(10) NOT NULL,
    num_doc VARCHAR(12) NOT NULL,
    fecha_comp DATE NOT NULL,
    tipo_doc VARCHAR(20) NOT NULL,
    fecha_reg DATE NOT NULL,
    CONSTRAINT upeu_compra_pk PRIMARY KEY (id_compra)
    );

CREATE TABLE IF NOT EXISTS upeu_cliente (
                                            dniruc VARCHAR(12) NOT NULL,
    nombres VARCHAR(60) NOT NULL,
    tipo_documento VARCHAR(12) NOT NULL,
    rep_legal VARCHAR(60) NOT NULL,
    CONSTRAINT upeu_cliente_pk PRIMARY KEY (dniruc)
    );

COMMENT ON COLUMN upeu_cliente.tipo_documento IS '--dni--ruc';

CREATE TABLE IF NOT EXISTS upeu_venta (
                                          id_venta IDENTITY NOT NULL,
                                          preciobase DOUBLE NOT NULL,
                                          igv DOUBLE NOT NULL,
                                          preciototal DOUBLE NOT NULL,
                                          dniruc VARCHAR(12) NOT NULL,
    id_usuario INTEGER NOT NULL,
    num_doc VARCHAR(40) NOT NULL,
    fecha_gener DATE NOT NULL,
    serie VARCHAR(10) NOT NULL,
    tipo_doc VARCHAR(20) NOT NULL,
    CONSTRAINT upeu_venta_pk PRIMARY KEY (id_venta)
    );

CREATE TABLE IF NOT EXISTS upeu_marca (
                                          id_marca IDENTITY NOT NULL,
                                          nombre VARCHAR(20) NOT NULL,
    CONSTRAINT upeu_marca_pk PRIMARY KEY (id_marca)
    );

CREATE TABLE IF NOT EXISTS upeu_categoria (
                                              id_categoria IDENTITY NOT NULL,
                                              nombre VARCHAR(20) NOT NULL,
    CONSTRAINT upeu_categoria_pk PRIMARY KEY (id_categoria)
    );

CREATE TABLE IF NOT EXISTS upeu_producto (
                                             id_producto IDENTITY NOT NULL,
                                             nombre VARCHAR(40) NOT NULL,
    pu DOUBLE NOT NULL,
    puold DOUBLE NOT NULL,
    utilidad DOUBLE NOT NULL,
    stock DOUBLE NOT NULL,
    stockold DOUBLE NOT NULL,
    id_categoria INTEGER NOT NULL,
    id_marca INTEGER NOT NULL,
    id_unidad INTEGER NOT NULL,
    CONSTRAINT upeu_producto_pk PRIMARY KEY (id_producto)
    );

CREATE TABLE IF NOT EXISTS upeu_venta_detalle (
                                                  id_venta_detalle IDENTITY NOT NULL,
                                                  pu DOUBLE NOT NULL,
                                                  cantidad DOUBLE NOT NULL,
                                                  descuento DOUBLE NOT NULL,
                                                  subtotal DOUBLE NOT NULL,
                                                  id_venta INTEGER NOT NULL,
                                                  id_producto INTEGER NOT NULL,
                                                  CONSTRAINT upeu_venta_detalle_pk PRIMARY KEY (id_venta_detalle)
    );

CREATE TABLE IF NOT EXISTS upeu_compra_detalle (
                                                   id_compra_detalle IDENTITY NOT NULL,
                                                   pu DOUBLE NOT NULL,
                                                   cantidad DOUBLE NOT NULL,
                                                   subtotal DOUBLE NOT NULL,
                                                   id_compra INTEGER NOT NULL,
                                                   id_producto INTEGER NOT NULL,
                                                   CONSTRAINT upeu_compra_detalle_pk PRIMARY KEY (id_compra_detalle)
    );

-- FOREIGN KEYS

ALTER TABLE upeu_usuario
    ADD CONSTRAINT IF NOT EXISTS upeu_perfil_usuario_fk
    FOREIGN KEY (id_perfil)
    REFERENCES upeu_perfil (id_perfil);

ALTER TABLE upeu_compra
    ADD CONSTRAINT IF NOT EXISTS upeu_proveedor_compra_fk
    FOREIGN KEY (id_proveedor)
    REFERENCES upeu_proveedor (id_proveedor);

ALTER TABLE upeu_producto
    ADD CONSTRAINT IF NOT EXISTS upeu_unid_medida_producto_fk
    FOREIGN KEY (id_unidad)
    REFERENCES upeu_unid_medida (id_unidad);

ALTER TABLE upeu_compra
    ADD CONSTRAINT IF NOT EXISTS upeu_usuario_compra_fk
    FOREIGN KEY (id_usuario)
    REFERENCES upeu_usuario (id_usuario);

ALTER TABLE upeu_venta
    ADD CONSTRAINT IF NOT EXISTS upeu_usuario_venta_fk
    FOREIGN KEY (id_usuario)
    REFERENCES upeu_usuario (id_usuario);

ALTER TABLE upeu_vent_carrito
    ADD CONSTRAINT IF NOT EXISTS upeu_usuario_carrito_fk
    FOREIGN KEY (id_usuario)
    REFERENCES upeu_usuario (id_usuario);

ALTER TABLE upeu_comp_carrito
    ADD CONSTRAINT IF NOT EXISTS upeu_usuario_comp_carrito_fk
    FOREIGN KEY (id_usuario)
    REFERENCES upeu_usuario (id_usuario);

ALTER TABLE upeu_compra_detalle
    ADD CONSTRAINT IF NOT EXISTS upeu_compra_compra_detalle_fk
    FOREIGN KEY (id_compra)
    REFERENCES upeu_compra (id_compra);

ALTER TABLE upeu_venta
    ADD CONSTRAINT IF NOT EXISTS upeu_cliente_venta_fk
    FOREIGN KEY (dniruc)
    REFERENCES upeu_cliente (dniruc);

ALTER TABLE upeu_venta_detalle
    ADD CONSTRAINT IF NOT EXISTS upeu_venta_venta_detalle_fk
    FOREIGN KEY (id_venta)
    REFERENCES upeu_venta (id_venta);

ALTER TABLE upeu_producto
    ADD CONSTRAINT IF NOT EXISTS upeu_marca_producto_fk
    FOREIGN KEY (id_marca)
    REFERENCES upeu_marca (id_marca);

ALTER TABLE upeu_producto
    ADD CONSTRAINT IF NOT EXISTS upeu_categoria_producto_fk
    FOREIGN KEY (id_categoria)
    REFERENCES upeu_categoria (id_categoria);

ALTER TABLE upeu_compra_detalle
    ADD CONSTRAINT IF NOT EXISTS upeu_producto_compra_detalle_fk
    FOREIGN KEY (id_producto)
    REFERENCES upeu_producto (id_producto);

ALTER TABLE upeu_venta_detalle
    ADD CONSTRAINT IF NOT EXISTS upeu_producto_venta_detalle_fk
    FOREIGN KEY (id_producto)
    REFERENCES upeu_producto (id_producto);


MERGE INTO upeu_perfil (id_perfil, nombre, codigo)
    KEY(id_perfil) VALUES (1, 'Root', 'ROOT'),
    (2, 'Administrador', 'ADM'),
    (3, 'Reporte', 'REP');

MERGE INTO upeu_usuario (id_usuario, usuario, clave, estado, id_perfil)
    KEY(id_usuario) VALUES (1, 'admin', 'admin123', 'ACTIVO', 1);

MERGE INTO upeu_categoria(id_categoria, nombre) KEY(id_categoria) VALUES(1,'Zapallizas'),(2,'Chompas'),(3,'Pantalones');

MERGE INTO upeu_marca(id_marca, nombre) KEY(id_marca) VALUES(1,'Adidas');

MERGE INTO upeu_unid_medida(id_unidad, nombre_medida) KEY(id_unidad) VALUES(1, 'Unidad');

ALTER TABLE upeu_producto ADD COLUMN IF NOT EXISTS talla VARCHAR(100) DEFAULT '';
ALTER TABLE upeu_producto ADD COLUMN IF NOT EXISTS color VARCHAR(100) DEFAULT '';
ALTER TABLE upeu_producto ALTER COLUMN talla VARCHAR(100);
ALTER TABLE upeu_producto ALTER COLUMN color VARCHAR(100);

MERGE INTO upeu_producto(id_producto, nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, talla, color) 
KEY(id_producto) VALUES 
(1, 'chompa', 50.0, 45.0, 5.0, 5.0, 5.0, 2, 1, 1, 'M', 'Negro'),
(2, 'chompa', 50.0, 45.0, 5.0, 4.0, 4.0, 2, 1, 1, 'S', 'Rojo'),
(3, 'pantalon', 80.0, 70.0, 10.0, 5.0, 5.0, 3, 1, 1, '32', 'Negro'),
(4, 'pantalon', 80.0, 70.0, 10.0, 7.0, 7.0, 3, 1, 1, '34', 'Azul');
