package com.demo.fde.models.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.demo.fde.models.entity.Archivo;

public interface IArchivoService {

	public List<Archivo> findAll();
	
	public Page<Archivo> findAll(Pageable pageable);
	
	public Archivo findById(Long id);
	
	public Archivo save(Archivo archivo);
	
	public void delete(Long id);
}
