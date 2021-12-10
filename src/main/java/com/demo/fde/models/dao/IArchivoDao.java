package com.demo.fde.models.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.fde.models.entity.Archivo;

public interface IArchivoDao extends JpaRepository<Archivo, Long> {

}
