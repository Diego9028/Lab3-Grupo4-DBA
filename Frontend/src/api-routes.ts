const API_ROUTES = {
    // Autenticación - Rutas base
    AUTH: '/auth',
    
    // Entidades principales - Solo rutas base
    CLIENTE: '/cliente',
    REPARTIDOR: '/repartidor', 
    EMPRESA: '/empresaAsociada',
    PEDIDO: '/pedido',
    ZONA_COBERTURA: '/zonaCobertura',
    URGENCIA: '/urgencia',
    
    // MongoDB Collections - Solo rutas base
    OPINIONES_CLIENTES: '/opiniones-clientes',
    HISTORIAL_REPARTIDORES: '/historial-repartidores',
    LOGS: '/logs',
    NAVEGACION: '/navegacion',
    
    // Otras entidades - Solo rutas base
    CALIFICACION: '/calificacion',
    CATEGORIA: '/categoria',
    DETALLE_PEDIDO: '/detallePedido',
    MEDIO_PAGO: '/mediopago',
    PEDIDO_PRODUCTO: '/pedidoProducto',
    PRODUCTO_SERVICIO: '/productoServicio'
}

export default API_ROUTES