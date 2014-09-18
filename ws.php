<?php
require_once('php/setupdb.php');

$tipo=$_REQUEST['tipo'];


if(!strcasecmp($tipo, "hacer_ruta")){
	
	$idTelefono=$_POST['IdTelefono'];
	$bus  = R::findOne( BUSES, " id_telefono = ? ", [ $idTelefono ]);	
	//$barray=R::getAll( 'SELECT * FROM '.BUSES.' WHERE idTelefono LIKE `'.$idTelefono.'` LIMIT 1' );
	//$buses=R::convertToBeans( BUSES, $barray );
	
	
	if($bus==NULL){
		$bus = R::dispense(BUSES);
		
		$bus->idTelefono=$_POST['IdTelefono'];
		$bus->nombre=$_POST['Nombre'];
		$bus->codigo=$_POST['Codigo'];
		$bus->mensaje=$_POST['Mensaje'];
		
		$lat=$_POST['Latitud'];
		$lon=$_POST['Longitud'];
		$posiciones=array();
		$posiciones[0]=array($lat,$lon);
		$bus->posiciones="$lat,$lon";
		
		R::store($bus);	
		
	}else{

		$bus->nombre=$_POST['Nombre'];
		$bus->codigo=$_POST['Codigo'];
		$bus->mensaje=$_POST['Mensaje'];
		

		$lat=$_POST['Latitud'];
		$lon=$_POST['Longitud'];
		$posiciones=$bus->posiciones.":$lat,$lon";
		$bus->posiciones=$posiciones;
		
		R::store($bus);	
	}
	
}else if(!strcasecmp($tipo, "eliminar_ruta")){
	
	$codigo=$_POST['Codigo'];
	$ruta  = R::findOne( RUTAS, ' codigo = '.$codigo);
	R::trash($ruta);
	
}else if(!strcasecmp($tipo, "dejar_ruta")){
	
	$idTelefono=$_POST['IdTelefono'];
	$buses  = R::findAll( BUSES, ' IdTelefono = '.$idTelefono);
	R::trashAll($buses);
	
}else if(!strcasecmp($tipo, "get_rutas")){
	$rutas = R::findAll(RUTAS);
	$resp=array();
	foreach($rutas as $r){
		$ru = new stdClass();
		$ru->Nombre=$r->nombre;
		$ru->Codigo=$r->codigo;
		$ru->Clase=$r->clase;
		$ru->HoraInicio=$r->inicio;
		$ru->HoraFin=$r->fin;
		$ru->FrecuenciaDePaso=$r->freq;
		$ru->Periodicidad=$r->period;
		$ru->ListaDeParadas=$r->paradas;
		$ru->Incidencias=$r->incidencias;
		
		$resp[]=$ru;
	}
	echo json_encode($resp);
}else if(!strcasecmp($tipo, "guardar_ruta")){
	//check si existe codigo
	$ruta = R::dispense(RUTAS);

	$ruta->nombre=$_POST['Nombre'];
	$ruta->codigo=$_POST['Codigo'];
	$ruta->clase=$_POST['Clase'];
	$ruta->inicio=$_POST['HoraInicio'];
	$ruta->fin=$_POST['HoraFin'];
	$ruta->freq=$_POST['FrecuenciaDePaso'];
	$ruta->period=$_POST['Periodicidad'];
    $ruta->incidencias="-";
	
	$paradas=json_decode($_POST['ListaDeParadas']);
	
	foreach($paradas as $p){
		$ruta->paradas.=$p->lat.",".$p->lon.":";	
	}
	$ruta->paradas=rtrim($ruta->paradas,":");
	
	$ruta->incidencias=$_POST['Incidencias'];

	R::store($ruta);

}else if(!strcasecmp($tipo, "ver_buses")){
	//check si existe codigo
    $codigo_ruta=$_REQUEST['Codigo'];
    $buses= R::findAll( BUSES, ' codigo = '.$codigo_ruta);

    $resp=array();
    foreach($buses as $bus){
        $b = new stdClass();
        $b->Nombre=$bus->nombre;
        $b->IdTelefono=$bus->id_telefono;
        $b->Codigo=$bus->codigo;
        $b->Mensaje=$bus->mensaje;
        $b->Posiciones=$bus->posiciones;
        $resp[]=$b;
    }
    echo json_encode($resp);
}


?>