class BluetoothDevice {
  BluetoothDevice({
    this.name,
    this.address,
    this.type,
    this.connected,
  });

  String? name;
  String? address;
  int? type = 0;
  bool? connected = false;

  factory BluetoothDevice.fromJson(Map<String, dynamic> json) {
    return BluetoothDevice(
      name: json['name'],
      address: json['address'],
      type: json['type'],
      connected: json['connected'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'address': address,
      'type': type,
      'connected': connected,
    };
  }
}
