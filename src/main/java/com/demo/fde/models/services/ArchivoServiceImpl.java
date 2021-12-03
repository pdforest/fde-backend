package com.demo.fde.models.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.fde.models.dao.IArchivoDao;
import com.demo.fde.models.entity.Archivo;

@Service
public class ArchivoServiceImpl implements IArchivoService {

	@Autowired
	private IArchivoDao archivoDao;
	
	@Override
	@Transactional(readOnly = true)
	public List<Archivo> findAll() {
		return (List<Archivo>) archivoDao.findAll();
	}

	@Override
	public Page<Archivo> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Archivo findById(Long id) {
		return archivoDao.findById(id).orElse(null);
	}


	@Override
	@Transactional
	public Archivo save(Archivo archivo) {
		return archivoDao.save(archivo);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub

	}

}
