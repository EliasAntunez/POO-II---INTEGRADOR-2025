    package com.example.facturacion.servicio;

    import java.math.BigDecimal;
    import java.util.List;
    import java.util.Objects;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import com.example.facturacion.modelo.Cliente;
    import com.example.facturacion.modelo.MovimientoCuentaCorriente;
    import com.example.facturacion.modelo.enums.EstadoCliente;
    import com.example.facturacion.repositorio.RepositorioCliente;
    import com.example.facturacion.repositorio.RepositorioMovimientoCuentaCorriente;

    /**
     * Servicio para manejar la lógica de negocio relacionada con los clientes.
     * HU-01: Alta de Cliente
     * HU-02: Modificación de Cliente
     * HU-03: Baja de Cliente
     * HU-05: Gestión de Cuenta Corriente
     */
    @Service
    public class ServicioCliente {
        
        @Autowired
        private RepositorioCliente repositorioCliente;
        
        @Autowired
        private RepositorioMovimientoCuentaCorriente repositorioMovimiento;

        // ==================== HU-01: Alta de Cliente ====================
        
        /**
         * Guarda un nuevo cliente. Valida unicidad de DNI, CUIT, email y teléfono.
         * Inicializa la cuenta corriente en cero.
         * @param cliente El cliente a guardar.
         * @return El cliente guardado.
         */
        @Transactional
        public Cliente guardarCliente(Cliente cliente) {
            validarDatosUnicos(cliente);
            
            // Asegurar que el cliente empiece con estado ACTIVO
            if (cliente.getEstado() == null) {
                cliente.setEstado(EstadoCliente.ACTIVO);
            }
            
            // Inicializar cuenta corriente en cero
            if (cliente.getSaldoCuentaCorriente() == null) {
                cliente.setSaldoCuentaCorriente(BigDecimal.ZERO);
            }
            
            return repositorioCliente.save(cliente);
        }

        /**
         * Valida que DNI, CUIT, email y teléfono sean únicos.
         */
        private void validarDatosUnicos(Cliente cliente) {
            if (repositorioCliente.existsByDni(cliente.getDni())) {
                throw new IllegalArgumentException("El DNI " + cliente.getDni() + " ya existe");
            }
            if (repositorioCliente.existsByCuit(cliente.getCuit())) {
                throw new IllegalArgumentException("El CUIT " + cliente.getCuit() + " ya existe");
            }
            if (repositorioCliente.existsByEmail(cliente.getEmail())) {
                throw new IllegalArgumentException("El email " + cliente.getEmail() + " ya existe");
            }
            if (repositorioCliente.existsByTelefono(cliente.getTelefono())) {
                throw new IllegalArgumentException("El teléfono " + cliente.getTelefono() + " ya existe");
            }
        }

        // ==================== HU-02: Modificación de Cliente ====================
        
        /**
         * Actualiza un cliente existente.
         * Valida que DNI, CUIT y condición fiscal no cambien (según HU-02).
         * @param cliente El cliente a actualizar.
         * @return El cliente actualizado.
         */
        @Transactional
        public Cliente actualizarCliente(Cliente cliente) {
            final Long id = Objects.requireNonNull(cliente.getId(), 
                "El ID del cliente es requerido para actualizar");
            
            // Verificar que el cliente existe
            Cliente clienteExistente = repositorioCliente.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "El cliente con ID " + id + " no existe"));
            
            // HU-02: DNI y CUIT no pueden modificarse
            if (!clienteExistente.getDni().equals(cliente.getDni())) {
                throw new IllegalArgumentException(
                    "El DNI no puede ser modificado (valor actual: " + clienteExistente.getDni() + ")");
            }
            if (!clienteExistente.getCuit().equals(cliente.getCuit())) {
                throw new IllegalArgumentException(
                    "El CUIT no puede ser modificado (valor actual: " + clienteExistente.getCuit() + ")");
            }
            
            // Validar unicidad de email y teléfono (excluyendo el ID actual)
            if (repositorioCliente.existsByEmailAndIdNot(cliente.getEmail(), id)) {
                throw new IllegalArgumentException(
                    "El email " + cliente.getEmail() + " ya existe en otro cliente");
            }
            if (repositorioCliente.existsByTelefonoAndIdNot(cliente.getTelefono(), id)) {
                throw new IllegalArgumentException(
                    "El teléfono " + cliente.getTelefono() + " ya existe en otro cliente");
            }
            
            // Preservar el saldo de cuenta corriente (no se modifica en edición simple)
            cliente.setSaldoCuentaCorriente(clienteExistente.getSaldoCuentaCorriente());
            
            return repositorioCliente.save(cliente);
        }

        // ==================== HU-03: Baja de Cliente ====================
        
        /**
         * Da de baja un cliente por su ID (baja lógica).
         * HU-03: Verifica que no tenga transacciones en proceso.
         * @param id El ID del cliente a dar de baja.
         */
        @Transactional
        public void darDeBajaClientePorId(Long id) {
            final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
            
            Cliente cliente = repositorioCliente.findById(safeId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "El cliente con ID " + safeId + " no existe"));
            
            // HU-03: Verificar que no tenga deuda pendiente (transacciones en proceso)
            if (cliente.tieneDeuda()) {
                throw new IllegalArgumentException(
                    "No se puede dar de baja el cliente con ID " + safeId + 
                    " porque tiene una deuda pendiente de $" + cliente.getMontoDeuda());
            }
            
            // Verificar facturas pendientes (podría implementarse en el futuro)
            // Por ahora, solo validamos el saldo
            
            cliente.darDeBaja();
            repositorioCliente.save(cliente);
        }
        
        /**
         * Suspende un cliente temporalmente.
         */
        @Transactional
        public void suspenderCliente(Long id) {
            Cliente cliente = obtenerClientePorId(id);
            cliente.suspender();
            repositorioCliente.save(cliente);
        }
        
        /**
         * Reactiva un cliente suspendido o dado de baja.
         */
        @Transactional
        public void reactivarCliente(Long id) {
            Cliente cliente = obtenerClientePorId(id);
            cliente.activar();
            repositorioCliente.save(cliente);
        }

        // ==================== Consultas ====================
        
        /**
         * Obtiene todos los clientes activos.
         */
        @Transactional(readOnly = true)
        public List<Cliente> obtenerTodosLosClientes() {
            return repositorioCliente.findByActivoTrue();
        }

        /**
         * Obtiene una página de clientes activos.
         */
        @Transactional(readOnly = true)
        public Page<Cliente> obtenerClientesPaginados(int page, int size) {
            final int safePage = Math.max(0, page);
            final int safeSize = Math.max(1, Math.min(size, 100)); // Máximo 100 por página
            Pageable pageable = PageRequest.of(safePage, safeSize, 
                Sort.by(Sort.Direction.DESC, "id"));
            return repositorioCliente.findByActivoTrue(pageable);
        }
        
        /**
         * Obtiene todos los clientes (activos e inactivos).
         */
        @Transactional(readOnly = true)
        public Page<Cliente> obtenerTodosLosClientesPaginados(int page, int size) {
            final int safePage = Math.max(0, page);
            final int safeSize = Math.max(1, Math.min(size, 100));
            Pageable pageable = PageRequest.of(safePage, safeSize, 
                Sort.by(Sort.Direction.DESC, "id"));
            return repositorioCliente.findAll(pageable);
        }
        
        /**
         * Obtiene clientes por estado.
         */
        @Transactional(readOnly = true)
        public Page<Cliente> obtenerClientesPorEstado(EstadoCliente estado, int page, int size) {
            final int safePage = Math.max(0, page);
            final int safeSize = Math.max(1, Math.min(size, 100));
            Pageable pageable = PageRequest.of(safePage, safeSize, 
                Sort.by(Sort.Direction.DESC, "id"));
            return repositorioCliente.findByEstado(estado, pageable);
        }

        /**
         * Obtiene un cliente por su ID.
         */
        @Transactional(readOnly = true)
        public Cliente obtenerClientePorId(Long id) {
            final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
            return repositorioCliente.findById(safeId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cliente no encontrado con ID: " + safeId));
        }

        /**
         * Obtiene todos los clientes activos (usando el enum).
         */
        @Transactional(readOnly = true)
        public List<Cliente> obtenerClientesActivos() {
            return repositorioCliente.findClientesActivos();
        }
        
        /**
         * Busca clientes por texto (nombre, apellido, DNI, CUIT, razón social).
         */
        @Transactional(readOnly = true)
        public Page<Cliente> buscarClientes(String busqueda, int page, int size) {
            final int safePage = Math.max(0, page);
            final int safeSize = Math.max(1, Math.min(size, 100));
            Pageable pageable = PageRequest.of(safePage, safeSize, 
                Sort.by(Sort.Direction.DESC, "id"));
            return repositorioCliente.buscarClientes(busqueda, pageable);
        }

        // ==================== Cuenta Corriente ====================
        
        /**
         * Obtiene el saldo de cuenta corriente de un cliente.
         */
        @Transactional(readOnly = true)
        public BigDecimal obtenerSaldoCuentaCorriente(Long clienteId) {
            Cliente cliente = obtenerClientePorId(clienteId);
            return cliente.getSaldoCuentaCorriente();
        }
        
        /**
         * Obtiene los movimientos de cuenta corriente de un cliente.
         */
        @Transactional(readOnly = true)
        public List<MovimientoCuentaCorriente> obtenerMovimientosCliente(Long clienteId) {
            Cliente cliente = obtenerClientePorId(clienteId);
            // ANTES: return cliente.getMovimientos(); 
            // AHORA: Buscamos directo en la tabla de movimientos
            return repositorioMovimiento.findByClienteOrderByFechaMovimientoDesc(cliente);
        }
        
        /**
         * Registra un movimiento manual en la cuenta corriente.
         */
        @Transactional
        public MovimientoCuentaCorriente registrarMovimiento(Long clienteId, MovimientoCuentaCorriente movimiento) {
            Cliente cliente = obtenerClientePorId(clienteId);
            
            // 1. Agregar a la lista y actualizar el saldo en memoria
            cliente.registrarMovimiento(movimiento);
            
            // 2. Guardar Cliente
            // Al tener CascadeType.ALL, esto actualiza el saldo del cliente en la DB
            // Y TAMBIÉN inserta el movimiento automáticamente.
            repositorioCliente.save(cliente);
            
            return movimiento; 
            // NO llamamos a repositorioMovimiento.save(movimiento) aquí.
        }
        
        /**
         * Recalcula el saldo de cuenta corriente de un cliente.
         */
        @Transactional
        public void recalcularSaldoCliente(Long clienteId) {
            Cliente cliente = obtenerClientePorId(clienteId);
            cliente.recalcularSaldo();
            repositorioCliente.save(cliente);
        }
        
        /**
         * Obtiene clientes con deuda.
         */
        @Transactional(readOnly = true)
        public List<Cliente> obtenerClientesConDeuda() {
            return repositorioCliente.findClientesConDeuda();
        }
        
        /**
         * Obtiene clientes con saldo a favor.
         */
        @Transactional(readOnly = true)
        public List<Cliente> obtenerClientesConSaldoAFavor() {
            return repositorioCliente.findClientesConSaldoAFavor();
        }

        // ==================== Estadísticas ====================
        
        /**
         * Cuenta el total de clientes activos.
         */
        @Transactional(readOnly = true)
        public long contarClientesActivos() {
            return repositorioCliente.findClientesActivos().size();
        }
        
        /**
         * Cuenta clientes por estado.
         */
        @Transactional(readOnly = true)
        public long contarClientesPorEstado(EstadoCliente estado) {
            return repositorioCliente.findByEstado(estado).size();
        }
    }