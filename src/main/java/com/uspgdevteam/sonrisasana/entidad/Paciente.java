package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pacientes")
public class Paciente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 20)
    private String dpi;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String direccion;

    @Column(length = 250)
    private String alergias;

    @Column(name = "condiciones_medicas", length = 300)
    private String condicionesMedicas;

    @Column(length = 300)
    private String observaciones;

    @Column(name = "fecha_creacion_expediente", nullable = false)
    private LocalDateTime fechaCreacionExpediente;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    // =====================================================
    // RELACIONES
    // =====================================================

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
    private List<Cita> citas;

    @OneToMany(mappedBy = "paciente",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ArchivoClinico> archivosClinicos;

    // =====================================================
    // CONSTRUCTORES
    // =====================================================

    public Paciente() {}

    // =====================================================
    // CALLBACKS
    // =====================================================

    @PrePersist
    public void prePersist() {
        this.fechaCreacionExpediente = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getCondicionesMedicas() { return condicionesMedicas; }
    public void setCondicionesMedicas(String condicionesMedicas) { this.condicionesMedicas = condicionesMedicas; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacionExpediente() { return fechaCreacionExpediente; }
    public void setFechaCreacionExpediente(LocalDateTime fechaCreacionExpediente) { this.fechaCreacionExpediente = fechaCreacionExpediente; }

    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    public List<Cita> getCitas() { return citas; }
    public void setCitas(List<Cita> citas) { this.citas = citas; }

    public List<ArchivoClinico> getArchivosClinicos() { return archivosClinicos; }
    public void setArchivosClinicos(List<ArchivoClinico> archivosClinicos) { this.archivosClinicos = archivosClinicos; }
}
