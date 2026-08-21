package com.kynsoft.report.infrastructure.services;

import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsoft.report.domain.dto.FincaProductoDto;
import com.kynsoft.report.domain.dto.MovimientoStockDto;
import com.kynsoft.report.domain.dto.TipoMovimientoStock;
import com.kynsoft.report.domain.services.IFincaProductoService;
import com.kynsoft.report.infrastructure.entity.MovimientoStock;
import com.kynsoft.report.infrastructure.repository.command.MovimientoStockWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.AlmacenReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.FincaProductoReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.FincaReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.MovimientoStockReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.ProductoReadDataJPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimientoStockService Unit Tests")
class MovimientoStockServiceImplTest {

    @Mock
    private MovimientoStockWriteDataJPARepository repositoryCommand;

    @Mock
    private MovimientoStockReadDataJPARepository repositoryQuery;

    @Mock
    private FincaReadDataJPARepository fincaRepository;

    @Mock
    private ProductoReadDataJPARepository productoRepository;

    @Mock
    private FincaProductoReadDataJPARepository fincaProductoRepository;

    @Mock
    private AlmacenReadDataJPARepository almacenRepository;

    @Mock
    private IFincaProductoService fincaProductoService;

    @InjectMocks
    private MovimientoStockServiceImpl movimientoStockService;

    private UUID almacenId;
    private UUID fincaProductoId;
    private UUID fincaId;
    private UUID productoId;

    @BeforeEach
    void setUp() {
        almacenId = UUID.randomUUID();
        fincaProductoId = UUID.randomUUID();
        fincaId = UUID.randomUUID();
        productoId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("crearAjuste() Tests")
    class CrearAjusteTests {

        @Test
        @DisplayName("RN-04: Debe rechazar ajuste sin observaciones")
        void debeRechazarAjusteSinObservaciones() {
            // Act & Assert
            assertThrows(
                    BusinessNotFoundException.class,
                    () -> movimientoStockService.crearAjuste(
                            almacenId,
                            fincaProductoId,
                            TipoMovimientoStock.ENTRADA_AJUSTE,
                            10,
                            ""
                    )
            );
        }

        @Test
        @DisplayName("RN-04: Debe rechazar ajuste con observaciones null")
        void debeRechazarAjusteConObservacionesNull() {
            // Act & Assert
            assertThrows(
                    BusinessNotFoundException.class,
                    () -> movimientoStockService.crearAjuste(
                            almacenId,
                            fincaProductoId,
                            TipoMovimientoStock.ENTRADA_AJUSTE,
                            10,
                            null
                    )
            );
        }

        @Test
        @DisplayName("RN-05: Debe rechazar cantidad menor o igual a cero")
        void debeRechazarCantidadMenorOIgualACero() {
            // Act & Assert
            assertThrows(
                    BusinessNotFoundException.class,
                    () -> movimientoStockService.crearAjuste(
                            almacenId,
                            fincaProductoId,
                            TipoMovimientoStock.ENTRADA_AJUSTE,
                            0,
                            "Observacion de prueba"
                    )
            );

            assertThrows(
                    BusinessNotFoundException.class,
                    () -> movimientoStockService.crearAjuste(
                            almacenId,
                            fincaProductoId,
                            TipoMovimientoStock.ENTRADA_AJUSTE,
                            -5,
                            "Observacion de prueba"
                    )
            );
        }

        @Test
        @DisplayName("Debe rechazar tipo de movimiento no permitido para ajuste")
        void debeRechazarTipoNoPermitido() {
            // Act & Assert
            assertThrows(
                    BusinessNotFoundException.class,
                    () -> movimientoStockService.crearAjuste(
                            almacenId,
                            fincaProductoId,
                            TipoMovimientoStock.ENTRADA_PRODUCCION,
                            10,
                            "Observacion de prueba"
                    )
            );
        }

        @Test
        @DisplayName("RN-06: Debe rechazar salida que deja stock negativo")
        void debeRechazarSalidaQueDejaStockNegativo() {
            // Arrange
            FincaProductoDto fincaProductoDto = FincaProductoDto.builder()
                    .id(fincaProductoId)
                    .fincaId(fincaId)
                    .productoId(productoId)
                    .stock(30)
                    .build();

            when(fincaProductoService.getById(fincaProductoId)).thenReturn(fincaProductoDto);

            // Act & Assert
            assertThrows(
                    BusinessNotFoundException.class,
                    () -> movimientoStockService.crearAjuste(
                            almacenId,
                            fincaProductoId,
                            TipoMovimientoStock.SALIDA_AJUSTE,
                            50,
                            "Intento de salida excesiva"
                    )
            );
        }

        @Test
        @DisplayName("Debe crear ajuste de entrada correctamente")
        void debeCrearAjusteEntradaCorrectamente() {
            // Arrange
            FincaProductoDto fincaProductoDto = FincaProductoDto.builder()
                    .id(fincaProductoId)
                    .fincaId(fincaId)
                    .productoId(productoId)
                    .stock(100)
                    .build();

            when(fincaProductoService.getById(fincaProductoId)).thenReturn(fincaProductoDto);
            when(repositoryCommand.save(any(MovimientoStock.class))).thenAnswer(invocation -> {
                MovimientoStock saved = invocation.getArgument(0);
                return saved;
            });
            doNothing().when(fincaProductoService).ajusteManual(any(), anyInt(), anyString());

            // Act
            MovimientoStockDto result = movimientoStockService.crearAjuste(
                    almacenId,
                    fincaProductoId,
                    TipoMovimientoStock.ENTRADA_AJUSTE,
                    50,
                    "Ajuste por conteo fisico"
            );

            // Assert
            assertNotNull(result);
            assertEquals(100, result.getStockAnterior());
            assertEquals(150, result.getStockNuevo());
            assertEquals(50, result.getCantidad());
            assertEquals(TipoMovimientoStock.ENTRADA_AJUSTE, result.getTipo());

            verify(fincaProductoService).ajusteManual(fincaProductoId, 50, "Ajuste por conteo fisico");
        }

        @Test
        @DisplayName("Debe crear ajuste de salida correctamente")
        void debeCrearAjusteSalidaCorrectamente() {
            // Arrange
            FincaProductoDto fincaProductoDto = FincaProductoDto.builder()
                    .id(fincaProductoId)
                    .fincaId(fincaId)
                    .productoId(productoId)
                    .stock(100)
                    .build();

            when(fincaProductoService.getById(fincaProductoId)).thenReturn(fincaProductoDto);
            when(repositoryCommand.save(any(MovimientoStock.class))).thenAnswer(invocation -> {
                MovimientoStock saved = invocation.getArgument(0);
                return saved;
            });
            doNothing().when(fincaProductoService).ajusteManual(any(), anyInt(), anyString());

            // Act
            MovimientoStockDto result = movimientoStockService.crearAjuste(
                    almacenId,
                    fincaProductoId,
                    TipoMovimientoStock.SALIDA_AJUSTE,
                    30,
                    "Ajuste por merma"
            );

            // Assert
            assertNotNull(result);
            assertEquals(100, result.getStockAnterior());
            assertEquals(70, result.getStockNuevo());
            assertEquals(30, result.getCantidad());
            assertEquals(TipoMovimientoStock.SALIDA_AJUSTE, result.getTipo());

            verify(fincaProductoService).ajusteManual(fincaProductoId, -30, "Ajuste por merma");
        }
    }

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar movimiento por ID")
        void debeEncontrarMovimientoPorId() {
            // Arrange
            UUID movimientoId = UUID.randomUUID();
            MovimientoStock movimiento = new MovimientoStock();
            movimiento.setId(movimientoId);
            movimiento.setFincaProductoId(fincaProductoId);
            movimiento.setFincaId(fincaId);
            movimiento.setProductoId(productoId);
            movimiento.setTipo(TipoMovimientoStock.ENTRADA_AJUSTE);
            movimiento.setCantidad(10);
            movimiento.setStockAnterior(100);
            movimiento.setStockNuevo(110);

            when(repositoryQuery.findById(movimientoId)).thenReturn(Optional.of(movimiento));

            // Act
            MovimientoStockDto result = movimientoStockService.findById(movimientoId);

            // Assert
            assertNotNull(result);
            assertEquals(movimientoId, result.getId());
        }

        @Test
        @DisplayName("Debe lanzar excepcion si no encuentra movimiento")
        void debeLanzarExcepcionSiNoEncuentraMovimiento() {
            // Arrange
            UUID movimientoId = UUID.randomUUID();
            when(repositoryQuery.findById(movimientoId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    BusinessNotFoundException.class,
                    () -> movimientoStockService.findById(movimientoId)
            );
        }
    }

    @Nested
    @DisplayName("TipoMovimientoStock Tests")
    class TipoMovimientoStockTests {

        @Test
        @DisplayName("isEntrada() debe retornar true para tipos de entrada")
        void isEntradaDebeRetornarTrueParaEntradas() {
            assertTrue(TipoMovimientoStock.ENTRADA_PRODUCCION.isEntrada());
            assertTrue(TipoMovimientoStock.ENTRADA_AJUSTE.isEntrada());
            assertTrue(TipoMovimientoStock.TRANSFERENCIA_ENTRADA.isEntrada());
        }

        @Test
        @DisplayName("isSalida() debe retornar true para tipos de salida")
        void isSalidaDebeRetornarTrueParaSalidas() {
            assertTrue(TipoMovimientoStock.SALIDA_VENTA.isSalida());
            assertTrue(TipoMovimientoStock.SALIDA_AUTOCONSUMO.isSalida());
            assertTrue(TipoMovimientoStock.SALIDA_AJUSTE.isSalida());
            assertTrue(TipoMovimientoStock.TRANSFERENCIA_SALIDA.isSalida());
        }

        @Test
        @DisplayName("isAjuste() debe retornar true para tipos de ajuste")
        void isAjusteDebeRetornarTrueParaAjustes() {
            assertTrue(TipoMovimientoStock.ENTRADA_AJUSTE.isAjuste());
            assertTrue(TipoMovimientoStock.SALIDA_AJUSTE.isAjuste());
            assertTrue(TipoMovimientoStock.AJUSTE_MANUAL.isAjuste());
        }
    }
}
