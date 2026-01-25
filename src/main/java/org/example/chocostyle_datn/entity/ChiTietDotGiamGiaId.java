package org.example.chocostyle_datn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietDotGiamGiaId implements Serializable {
    private static final long serialVersionUID = -771419114812568067L;
    @NotNull
    @Column(name = "id_dot_giam_gia", nullable = false)
    private Integer idDotGiamGia;

    @NotNull
    @Column(name = "id_spct", nullable = false)
    private Integer idSpct;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ChiTietDotGiamGiaId entity = (ChiTietDotGiamGiaId) o;
        return Objects.equals(this.idDotGiamGia, entity.idDotGiamGia) &&
                Objects.equals(this.idSpct, entity.idSpct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDotGiamGia, idSpct);
    }

}