<?php 
	error_reporting(E_ALL);
	ini_set('display_errors', 1);
	require_once('php/setupdb.php');
	
	if(isset($_GET['delete'])){
		$id=$_GET['delete'];
		$ruta = R::load(RUTAS, $id );
		R::trash($ruta);
	}
	
	if(isset($_POST['form'])){
		$ruta = R::dispense(RUTAS);
		
		$ruta->nombre=$_POST['nombre'];
		$ruta->codigo=$_POST['codigo'];
		$ruta->clase=$_POST['clase'];
		$ruta->inicio=$_POST['inicio'];
		$ruta->fin=$_POST['fin'];
		$ruta->freq=$_POST['freq'];
		$ruta->period=$_POST['period'];
		$ruta->paradas=$_POST['paradas'];
		$ruta->incidencias=$_POST['incidencias'];	
		
		R::store($ruta);	
	}else if(isset($_POST['form_modify'])){
	
		$id=$_POST['form_modify'];
	
		$ruta = R::load(RUTAS,$id);
		
		$ruta->nombre=$_POST['nombre'];
		$ruta->codigo=$_POST['codigo'];
		$ruta->clase=$_POST['clase'];
		$ruta->inicio=$_POST['inicio'];
		$ruta->fin=$_POST['fin'];
		$ruta->freq=$_POST['freq'];
		$ruta->period=$_POST['period'];
		$ruta->paradas=$_POST['paradas'];
		$ruta->incidencias=$_POST['incidencias'];	
		
		R::store($ruta);	
	}
	
?>


<html>
	<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
	
	
	
	</head>
	
	
	<body>
		<div class="container-fluid">
		<div class="row">
			<div class="col-xs-1"></div>
			<div class="col-xs-10">
		
		<table class="table table-condensed table-striped">
			<tr>
				
				<td>
					Nombre
				</td>
				
				<td>
					Codigo
				</td>
				
				<td>
					Clase
				</td>
				
				<td>
					Hora Inicio
				</td>
				
				<td>
					Hora fin
				</td>
				
				<td>
					Frecuencia de paso
				</td>
				
				<td>
					Periodicidad
				</td>
				
				<td>
					Lista de paradas
				</td>
				
				<td>
					Incidencias
				</td>
				<td>
					Modificar
				</td>
				
				<td>
					Eliminar
				</td>
				
				

				
			</tr>
			
			<?php
			
			
			$rutas = R::findAll(RUTAS);
				foreach($rutas as $ruta) {
					echo '
					
					<tr>
				
				<td>
					'.$ruta->nombre.'
				</td>
				
				<td>
					'.$ruta->codigo.'
				</td>
				
				<td>
					'.$ruta->clase.'
				</td>
				
				<td>
					'.$ruta->inicio.'
				</td>
				
				<td>
					'.$ruta->fin.'
				</td>
				
				<td>
					'.$ruta->freq.'
				</td>
				
				<td>
					'.$ruta->period.'
				</td>
				
				<td>
					'.str_replace(":", "<br/>", $ruta->paradas).'
				</td>
				
				<td>
					'.$ruta->incidencias.'
				</td>
				<td><a href="rutas.php?modify='.$ruta->id.'"><button class="btn btn-default">Modificar</button></a></td><td><a href="rutas.php?delete='.$ruta->id.'"><button class="btn btn-default">Eliminar</button></a></td>
				
			</tr>
					
					';
				}
			
			
			?>
			

			
		</table>
		
		<br/>
		<button id="add" class="btn btn-default" value="Añadir" >Añadir</button>		
		
		
		<br/><br/>
		
<div id="nuevo" style="display:none;">
<form method="post" action="rutas.php">
<input type="hidden" name="form"/>
		<div class="input-group input-group-sm">

		<input class="form-control" type="text" name="nombre" placeholder="Nombre"/>
				
				<br/><br/>
					<input class="form-control" type="text" name="codigo" placeholder="Codigo"/>
				<br/><br/>
					<input class="form-control" type="text" name="clase" placeholder="Clase"/>
				<br/><br/>

					<input class="form-control" type="text" name="inicio" placeholder="Inicio"/>
				<br/><br/>

					<input class="form-control" type="text" name="fin" placeholder="Fin"/>
								<br/><br/>
					<input class="form-control" type="text" name="freq" placeholder="Frecuencia"/>
				<br/><br/>
					<input class="form-control" type="text" name="period" placeholder="Periodo"/>
				<br/><br/>
					<textarea class="form-control" name="paradas" placeholder="Paradas"></textarea>
								<br/><br/>
					<input class="form-control" type="text" name="incidencias" placeholder="Incidencias"/>

		</div>
		<br/>
		<button type="submit" class="btn btn-default" >Guardar</button>
</form>
</div>


<?php 
	if(isset($_GET['modify'])){
		$id=$_GET['modify'];
		$ruta = R::load(RUTAS, $id );
		
		echo '
		
<form method="post" action="rutas.php">
<input type="hidden" name="form_modify" value="'.$ruta->id.'"/>
		<div class="input-group input-group-sm">

		<input class="form-control" type="text" name="nombre" placeholder="Nombre" value="'.$ruta->nombre.'"/>
				
				<br/><br/>
					<input class="form-control" type="text" name="codigo" placeholder="Codigo" value="'.$ruta->codigo.'"/>
				<br/><br/>
					<input class="form-control" type="text" name="clase" placeholder="Clase" value="'.$ruta->clase.'"/>
				<br/><br/>

					<input class="form-control" type="text" name="inicio" placeholder="Inicio" value="'.$ruta->inicio.'"/>
				<br/><br/>

					<input class="form-control" type="text" name="fin" placeholder="Fin" value="'.$ruta->fin.'"/>
								<br/><br/>
					<input class="form-control" type="text" name="freq" placeholder="Frecuencia" value="'.$ruta->freq.'"/>
				<br/><br/>
					<input class="form-control" type="text" name="period" placeholder="Periodo" value="'.$ruta->period.'"/>
				<br/><br/>
					<textarea class="form-control" type="text" name="paradas" placeholder="Paradas" >'.$ruta->paradas.'</textarea>
								<br/><br/>
					<input class="form-control" type="text" name="incidencias" placeholder="Incidencias" value="'.$ruta->incidencias.'"/>

		</div>
		<br/>
		<button type="submit" class="btn btn-default" >Modificar</button>
</form>

		
		';
		
	}
	
	
?>
		
		
			</div>
			<div class="col-xs-1"></div>
		</div>
		</div>
		
		
		<script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
		<script src="js/rutas.js"></script>
		
	</body>
	
	
</html>