/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Documents and Settings\\yang.li2\\Desktop\\debug\\FMRadio\\src\\com\\mediatek\\FMRadio\\IFMRadioService.aidl
 */
package com.mediatek.FMRadio;
public interface IFMRadioService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mediatek.FMRadio.IFMRadioService
{
private static final java.lang.String DESCRIPTOR = "com.mediatek.FMRadio.IFMRadioService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mediatek.FMRadio.IFMRadioService interface,
 * generating a proxy if needed.
 */
public static com.mediatek.FMRadio.IFMRadioService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mediatek.FMRadio.IFMRadioService))) {
return ((com.mediatek.FMRadio.IFMRadioService)iin);
}
return new com.mediatek.FMRadio.IFMRadioService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_openDevice:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.openDevice();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_closeDevice:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.closeDevice();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isDeviceOpen:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isDeviceOpen();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_powerUp:
{
data.enforceInterface(DESCRIPTOR);
float _arg0;
_arg0 = data.readFloat();
boolean _result = this.powerUp(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_powerDown:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.powerDown();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isPowerUp:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPowerUp();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_powerUpAsync:
{
data.enforceInterface(DESCRIPTOR);
float _arg0;
_arg0 = data.readFloat();
this.powerUpAsync(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_powerDownAsync:
{
data.enforceInterface(DESCRIPTOR);
this.powerDownAsync();
reply.writeNoException();
return true;
}
case TRANSACTION_tune:
{
data.enforceInterface(DESCRIPTOR);
float _arg0;
_arg0 = data.readFloat();
boolean _result = this.tune(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_seek:
{
data.enforceInterface(DESCRIPTOR);
float _arg0;
_arg0 = data.readFloat();
boolean _arg1;
_arg1 = (0!=data.readInt());
float _result = this.seek(_arg0, _arg1);
reply.writeNoException();
reply.writeFloat(_result);
return true;
}
case TRANSACTION_setMute:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _result = this.setMute(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_useEarphone:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.useEarphone(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isEarphoneUsed:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isEarphoneUsed();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_initService:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.initService(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isServiceInit:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isServiceInit();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getFrequency:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getFrequency();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_resumeFMAudio:
{
data.enforceInterface(DESCRIPTOR);
this.resumeFMAudio();
reply.writeNoException();
return true;
}
case TRANSACTION_switchAntenna:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _result = this.switchAntenna(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_seekStationAsync:
{
data.enforceInterface(DESCRIPTOR);
float _arg0;
_arg0 = data.readFloat();
boolean _arg1;
_arg1 = (0!=data.readInt());
this.seekStationAsync(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mediatek.FMRadio.IFMRadioService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public boolean openDevice() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_openDevice, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean closeDevice() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_closeDevice, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isDeviceOpen() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isDeviceOpen, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean powerUp(float frequency) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeFloat(frequency);
mRemote.transact(Stub.TRANSACTION_powerUp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean powerDown() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_powerDown, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isPowerUp() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPowerUp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void powerUpAsync(float frequency) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeFloat(frequency);
mRemote.transact(Stub.TRANSACTION_powerUpAsync, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void powerDownAsync() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_powerDownAsync, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean tune(float frequency) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeFloat(frequency);
mRemote.transact(Stub.TRANSACTION_tune, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public float seek(float frequency, boolean isUp) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
float _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeFloat(frequency);
_data.writeInt(((isUp)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_seek, _data, _reply, 0);
_reply.readException();
_result = _reply.readFloat();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setMute(boolean mute) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((mute)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setMute, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void useEarphone(boolean use) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((use)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_useEarphone, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean isEarphoneUsed() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isEarphoneUsed, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void initService(int iCurrentStation) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(iCurrentStation);
mRemote.transact(Stub.TRANSACTION_initService, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean isServiceInit() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isServiceInit, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getFrequency() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getFrequency, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void resumeFMAudio() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_resumeFMAudio, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int switchAntenna(int antenna) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(antenna);
mRemote.transact(Stub.TRANSACTION_switchAntenna, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void seekStationAsync(float frequency, boolean isUp) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeFloat(frequency);
_data.writeInt(((isUp)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_seekStationAsync, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_openDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_closeDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_isDeviceOpen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_powerUp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_powerDown = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_isPowerUp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_powerUpAsync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_powerDownAsync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_tune = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_seek = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_setMute = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_useEarphone = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_isEarphoneUsed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_initService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_isServiceInit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getFrequency = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_resumeFMAudio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_switchAntenna = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_seekStationAsync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
}
public boolean openDevice() throws android.os.RemoteException;
public boolean closeDevice() throws android.os.RemoteException;
public boolean isDeviceOpen() throws android.os.RemoteException;
public boolean powerUp(float frequency) throws android.os.RemoteException;
public boolean powerDown() throws android.os.RemoteException;
public boolean isPowerUp() throws android.os.RemoteException;
public void powerUpAsync(float frequency) throws android.os.RemoteException;
public void powerDownAsync() throws android.os.RemoteException;
public boolean tune(float frequency) throws android.os.RemoteException;
public float seek(float frequency, boolean isUp) throws android.os.RemoteException;
public int setMute(boolean mute) throws android.os.RemoteException;
public void useEarphone(boolean use) throws android.os.RemoteException;
public boolean isEarphoneUsed() throws android.os.RemoteException;
public void initService(int iCurrentStation) throws android.os.RemoteException;
public boolean isServiceInit() throws android.os.RemoteException;
public int getFrequency() throws android.os.RemoteException;
public void resumeFMAudio() throws android.os.RemoteException;
public int switchAntenna(int antenna) throws android.os.RemoteException;
public void seekStationAsync(float frequency, boolean isUp) throws android.os.RemoteException;
}
