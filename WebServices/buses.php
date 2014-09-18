<?php 
	error_reporting(E_ALL);
	ini_set('display_errors', 1);
	require_once('php/setupdb.php');
	
	if(isset($_GET['delete'])){
		$id=$_GET['delete'];
		$bus = R::load(BUSES, $id );
		R::trash($bus);
	}
	
	if(isset($_POST['form'])){
		$bus = R::dispense(BUSES);
		
		$bus->idTelefono=$_POST['idTelefono'];
		$bus->nombre=$_POST['nombre'];
		$bus->posiciones=$_POST['posiciones'];
		$bus->codigo=$_POST['codigo'];
		$bus->mensaje=$_POST['mensaje'];
		
		R::store($bus);	
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
					idTelefono
				</td>
				
				<td>
					Nombre
				</td>
				
				<td>
					Posiciones
				</td>
				
				<td>
					Codigo
				</td>
				
				<td>
					Mensaje
				</td>

				
				<td>
					Eliminar
				</td>
				
				

				
			</tr>
			
			<?php
			
			
			$buses = R::findAll(BUSES);
				foreach($buses as $bus) {
					echo '
					
					<tr>
				
				<td>
					'.$bus->idTelefono.'
				</td>
				
				<td>
					'.$bus->nombre.'
				</td>
				
				<td>
					'.str_replace(":", "<br/>", $bus->posiciones).'
				</td>
				
				<td>
					'.$bus->codigo.'
				</td>
				
				<td>
					'.$bus->mensaje.'
				</td>
				
				<td><a href="buses.php?delete='.$bus->id.'"><button class="btn btn-default">Eliminar</button></a></td>
				
			</tr>
					
					';
				}
			
			
			?>
			

			
		</table>
		
		<br/>
		


					</div>
			<div class="col-xs-1"></div>
		</div>
		</div>
		
		
		<script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
		<script src="js/buses.js"></script>
		
	</body>
	
	
</html>