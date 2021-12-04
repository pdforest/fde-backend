package com.demo.fde.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.demo.fde.models.entity.Archivo;
import com.demo.fde.models.services.IArchivoService;
import com.demo.fde.util.ZipUtil;

@RestController
@RequestMapping("/api")
public class ArchivoRestController {
	
	@Autowired
	private IArchivoService archivoService;
	
	private final Logger log = LoggerFactory.getLogger(ArchivoRestController.class);

	@Value("${carpeta.origen}")
	String carpetaOrigen;

	@Value("${carpeta.destino}")
	String carpetaDestino;
	
	@GetMapping("/archivos")
	public List<Archivo> index(){
		return archivoService.findAll();
	}
	
	@PostMapping("/archivos/upload")
	public ResponseEntity<?> upload(@RequestParam("zipfile") MultipartFile zipfile){
		
		Map<String, Object> response = new HashMap<>();
		Archivo archivoNuevo = null;
		Archivo archivoGrabado = null;
		
		if(!zipfile.isEmpty()) {
			String nombreArchivo = zipfile.getOriginalFilename();
			
			Path rutaArchivo = Paths.get("uploads/origen").resolve(nombreArchivo).toAbsolutePath();
			log.info(rutaArchivo.toString());
			
			try {
				Files.copy(zipfile.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				response.put("mensaje", "Error al subir el archivo: " + nombreArchivo);
				response.put("error", e.getMessage() + " : " + e.getCause().getMessage());
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			File archivoSubido = rutaArchivo.toFile();
			
			archivoNuevo = new Archivo();
			archivoNuevo.setNombre(nombreArchivo.substring(0, nombreArchivo.indexOf(".")));
			archivoNuevo.setExtension(nombreArchivo.substring(nombreArchivo.indexOf(".")+1));
			archivoNuevo.setFechaProceso(new Date());
			archivoNuevo.setTamano(archivoSubido.length());
			archivoNuevo.setEstado(0);  // 0 = nuevo no procesado
			
			archivoGrabado = archivoService.save(archivoNuevo);
				
			response.put("cliente", archivoGrabado);
			response.put("mensaje", "Ha subido correctamente el archivo: " + archivoGrabado.getNombre());
			
		}
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	
	@PutMapping("/archivos/unzip/{id}")
	public ResponseEntity<?> unzip(@PathVariable Long id) {
		
		Map<String, Object> response = new HashMap<>();

		Archivo actual = archivoService.findById(id);
				
		if(actual == null) {
			response.put("mensaje", "Error : El archivo con el ID: " + id.toString() + " no se encuentra en la base de datos");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		String nombreArchivo = actual.getNombre() + "." + actual.getExtension();
		log.info(carpetaOrigen + "/" + nombreArchivo);

		try {
			if((actual.getExtension().toLowerCase()).equals("zip")) {
				ZipUtil.decompressZip(carpetaOrigen + "/" + nombreArchivo, carpetaDestino);
				
			} else if((actual.getExtension().toLowerCase()).equals("7z")) {
				ZipUtil.decompress7z(carpetaOrigen + "/" + nombreArchivo, carpetaDestino);
			}
			
			//TO-DO actualizar estado en DB
			
		} catch (FileNotFoundException e) {
			response.put("mensaje", "Error no se encontro el archivo: " + nombreArchivo);
			response.put("error", e.getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			
		} catch (IOException e) {
			response.put("mensaje", "Error al descomprimir el archivo: " + nombreArchivo);
			response.put("error", e.getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (DataAccessException e) {
			response.put("mensaje", "Error al actualizar el archivo en la base de datos");
			response.put("error", e.getMessage() + " : " + e.getMostSpecificCause().getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El archivo " + nombreArchivo + " se proceso con exito");
		response.put("archivo", actual);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);

	}

}
